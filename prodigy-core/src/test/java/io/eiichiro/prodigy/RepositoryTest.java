package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.hamcrest.CoreMatchers.is;
import static org.powermock.api.mockito.PowerMockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.eiichiro.reverb.lang.UncheckedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("org.apache.logging.log4j.*")
@PrepareForTest(Repository.class)
public class RepositoryTest {

	@Test
	public void testLoad() throws Exception {
		// Cache directory does not exist
		// File saved (S3): repository-test-1.jar
		// File cached (local): nothing
		// - cache directory created
		// - repository-test-1.jar added, nothing removed and repository-test-1.jar loaded
		Configuration configuration = new Configuration("", "");
		Prodigy.configuration(configuration);
		AmazonS3 s3 = mock(AmazonS3.class);
		ListObjectsV2Result result = new ListObjectsV2Result();
		S3ObjectSummary objectSummary = new S3ObjectSummary();
		objectSummary.setKey("repository-test-1.jar");
		result.getObjectSummaries().add(objectSummary);
		result.setTruncated(false);
		doReturn(result).when(s3).listObjectsV2(anyString());
		Repository repository = spy(new Repository(s3));
		doReturn(false).when(repository, "exists", anyString());
		doNothing().when(repository, "create", anyString());
		List<String> files = new ArrayList<>();
		doReturn(files).when(repository, "list", anyString());
		doAnswer(i -> {
			Collection<String> add = i.getArgument(1);
			assertThat(add.size(), is(1));
			assertTrue(add.contains("repository-test-1.jar"));
			files.addAll(add);
			return null;
		}).when(repository, "create", anyString(), any(Collection.class));
		doAnswer(i -> {
			Collection<String> remove = i.getArgument(1);
			assertTrue(remove.isEmpty());
			files.removeAll(remove);
			return null;
		}).when(repository, "delete", anyString(), any(Collection.class));
		doAnswer(i -> {
			List<URL> urls = i.getArgument(0);
			assertThat(urls.size(), is(1));
			assertTrue(urls.get(0).toString().endsWith("repository-test-1.jar"));
			return Thread.currentThread().getContextClassLoader();
		}).when(repository, "newLoader", any(List.class));
		doAnswer(i -> {
			return new ArrayList<>();
		}).when(repository, "readFaults", any(ClassLoader.class));
		Map<String, Class<? extends Fault>> faults = repository.load();
		assertTrue(faults.isEmpty());
		verifyPrivate(repository, times(1)).invoke("create", anyString());

		// Cache directory exists
		// File saved (S3): nothing
		// File cached (local): repository-test-2.jar
		// - cache directory not created
		// - nothing added, repository-test-2.jar removed and nothing loaded
		s3 = mock(AmazonS3.class);
		result = new ListObjectsV2Result();
		result.setTruncated(false);
		doReturn(result).when(s3).listObjectsV2(anyString());
		repository = spy(new Repository(s3));
		doReturn(true).when(repository, "exists", anyString());
		doNothing().when(repository, "create", anyString());
		List<String> files2 = new ArrayList<>();
		files2.add("repository-test-2.jar");
		doReturn(files2).when(repository, "list", anyString());
		doAnswer(i -> {
			Collection<String> add = i.getArgument(1);
			assertTrue(add.isEmpty());
			files2.addAll(add);
			return null;
		}).when(repository, "create", anyString(), any(Collection.class));
		doAnswer(i -> {
			Collection<String> remove = i.getArgument(1);
			assertThat(remove.size(), is(1));
			assertTrue(remove.contains("repository-test-2.jar"));
			files2.removeAll(remove);
			return null;
		}).when(repository, "delete", anyString(), any(Collection.class));
		doAnswer(i -> {
			List<URL> urls = i.getArgument(0);
			assertTrue(urls.isEmpty());
			return Thread.currentThread().getContextClassLoader();
		}).when(repository, "newLoader", any(List.class));
		doAnswer(i -> {
			return new ArrayList<>();
		}).when(repository, "readFaults", any(ClassLoader.class));
		faults = repository.load();
		assertTrue(faults.isEmpty());
		verifyPrivate(repository, never()).invoke("create", anyString());

		// File saved (S3): repository-test-3.jar
		// File cached (local): repository-test-3.jar
		// - nothing added, nothing removed and repository-test-3.jar loaded
		s3 = mock(AmazonS3.class);
		result = new ListObjectsV2Result();
		objectSummary = new S3ObjectSummary();
		objectSummary.setKey("repository-test-3.jar");
		result.getObjectSummaries().add(objectSummary);
		result.setTruncated(false);
		doReturn(result).when(s3).listObjectsV2(anyString());
		repository = spy(new Repository(s3));
		doReturn(true).when(repository, "exists", anyString());
		doNothing().when(repository, "create", anyString());
		List<String> files3 = new ArrayList<>();
		files3.add("repository-test-3.jar");
		doReturn(files3).when(repository, "list", anyString());
		doAnswer(i -> {
			Collection<String> add = i.getArgument(1);
			assertTrue(add.isEmpty());
			files3.addAll(add);
			return null;
		}).when(repository, "create", anyString(), any(Collection.class));
		doAnswer(i -> {
			Collection<String> remove = i.getArgument(1);
			assertTrue(remove.isEmpty());
			files3.removeAll(remove);
			return null;
		}).when(repository, "delete", anyString(), any(Collection.class));
		doAnswer(i -> {
			List<URL> urls = i.getArgument(0);
			assertThat(urls.size(), is(1));
			assertTrue(urls.get(0).toString().endsWith("repository-test-3.jar"));
			return Thread.currentThread().getContextClassLoader();
		}).when(repository, "newLoader", any(List.class));
		doAnswer(i -> {
			return new ArrayList<>();
		}).when(repository, "readFaults", any(ClassLoader.class));
		faults = repository.load();
		assertTrue(faults.isEmpty());

		// Line starts with '#' - not loaded
		// Fault not found - not loaded
		// Fault does not inherit io.eiichiro.prodigy.Fault - not loaded
		// Fault with @Named - loaded with the specified name
		// Fault without @Named - loaded with the simple name
		s3 = mock(AmazonS3.class);
		result = new ListObjectsV2Result();
		objectSummary = new S3ObjectSummary();
		objectSummary.setKey("repository-test-4.jar");
		result.getObjectSummaries().add(objectSummary);
		result.setTruncated(false);
		doReturn(result).when(s3).listObjectsV2(anyString());
		repository = spy(new Repository(s3));
		doReturn(true).when(repository, "exists", anyString());
		doNothing().when(repository, "create", anyString());
		doReturn(new ArrayList<>()).when(repository, "list", anyString());
		doNothing().when(repository, "create", anyString(), any(Collection.class));
		doNothing().when(repository, "delete", anyString(), any(Collection.class));
		doReturn(Thread.currentThread().getContextClassLoader()).when(repository, "newLoader", any(List.class));
		List<String> classes = new ArrayList<>();
		classes.add("# io.eiichiro.prodigy.Fault1");
		classes.add("io.eiichiro.prodigy.FaultNotFound");
		classes.add("io.eiichiro.prodigy.Fault3");
		classes.add("io.eiichiro.prodigy.Fault4");
		classes.add("io.eiichiro.prodigy.Fault2");
		doReturn(classes).when(repository, "readFaults", any(ClassLoader.class));
		faults = repository.load();
		assertThat(faults.size(), is(2));
		assertTrue(faults.get("fault-4").equals(Fault4.class));
		assertTrue(faults.get("Fault2").equals(Fault2.class));

		// IOException thrown - the exception thrown 	again
		s3 = mock(AmazonS3.class);
		result = new ListObjectsV2Result();
		result.setTruncated(false);
		doReturn(result).when(s3).listObjectsV2(anyString());
		repository = spy(new Repository(s3));
		doReturn(false).when(repository, "exists", anyString());
		doThrow(new IOException("hello")).when(repository, "create", anyString());
		doReturn(new ArrayList<>()).when(repository, "list", anyString());
		doNothing().when(repository, "create", anyString(), any(Collection.class));
		doNothing().when(repository, "delete", anyString(), any(Collection.class));
		doReturn(Thread.currentThread().getContextClassLoader()).when(repository, "newLoader", any(List.class));
		classes = new ArrayList<>();
		doReturn(classes).when(repository, "readFaults", any(ClassLoader.class));

		try {
			faults = repository.load();
			fail();
		} catch (UncheckedException e) {
			assertThat(e.getMessage(), is("hello"));
		}
	}

	@Test
	public void testSave() {}

}
