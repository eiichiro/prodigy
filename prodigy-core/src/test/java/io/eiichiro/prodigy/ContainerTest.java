package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Test;

import io.eiichiro.prodigy.Scheduler.Entry;

public class ContainerTest {

	@Test
	public void testContainerSchedulerRepository() {
		// Repository throws exception - 'classes' is empty
		Repository repository = mock(Repository.class);
		doThrow(new IllegalStateException("hello")).when(repository).load();
		Scheduler scheduler = mock(Scheduler.class);
		Container container = new Container(scheduler, repository);
		assertTrue(container.classes().isEmpty());

		// Fault loaded - included in 'classes'
		Map<String, Class<? extends Fault>> classes = new HashMap<>();
		classes.put("Fault1", Fault1.class);
		classes.put("Interceptor1", Interceptor1.class);
		repository = mock(Repository.class);
		doReturn(classes).when(repository).load();
		container = new Container(scheduler, repository);
		assertThat(container.classes().size(), is(2));
		assertTrue(container.classes().get("Fault1").equals(Fault1.class));
		assertTrue(container.classes().get("Interceptor1").equals(Interceptor1.class));

		// Scheduler throws exception - both 'faults' and 'interceptors' are empty
		repository = mock(Repository.class);
		scheduler = mock(Scheduler.class);
		doThrow(new IllegalStateException("hello")).when(scheduler).list();
		container = new Container(scheduler, repository);
		assertTrue(container.faults().isEmpty());
		assertTrue(container.interceptors().isEmpty());

		// Fault not instantiated - not included in 'faults' and 'interceptors'
		// Fault is an Interceptor and status is 'ACTIVE' - included in 'faults' and 'interceptors'
		// Fault is an Interceptor and status is not 'ACTIVE' - included in 'faults' but not in 'interceptors'
		// Fault is not an Interceptor - included in 'faults' but not in 'interceptors'
		classes = new HashMap<>();
		classes.put("Fault1", Fault1.class);
		classes.put("Interceptor1", Interceptor1.class);
		repository = mock(Repository.class);
		doReturn(classes).when(repository).load();
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry("fault-id-1", "not-found", "ACTIVE", "{}"));
		entries.add(new Entry("fault-id-2", "Interceptor1", "ACTIVE", "{}"));
		entries.add(new Entry("fault-id-3", "Interceptor1", "INACTIVE", "{}"));
		entries.add(new Entry("fault-id-4", "Fault1", "INACTIVE", "{}"));
		doReturn(entries).when(scheduler).list();
		container = new Container(scheduler, repository);
		assertThat(container.faults().size(), is(3));
		assertThat(container.faults().get("fault-id-2"), instanceOf(Interceptor1.class));
		assertThat(container.faults().get("fault-id-3"), instanceOf(Interceptor1.class));
		assertThat(container.faults().get("fault-id-4"), instanceOf(Fault1.class));
		assertThat(container.interceptors().size(), is(1));
		assertThat(container.interceptors().get("fault-id-2"), instanceOf(Interceptor1.class));
	}

	@Test
	public void testFaultStringString() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException,
			SecurityException {
		// Fault with id of the length of 8
		Container container = new Container();
		Map<String, Class<? extends Fault>> classes = new HashMap<>();
		classes.put("fault-2", Fault2.class);
		Field classesField = Container.class.getDeclaredField("classes");
		classesField.setAccessible(true);
		classesField.set(container, classes);
		Fault2 fault2 = (Fault2) container.fault("fault-2", "{\"property2\":2,\"property3\":\"hello\"}");
		assertThat(fault2.id().length(), is(8));
	}

	@Test
	public void testFaultStringStringString() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		// Fault not found - null returned
		Container container = new Container();
		Map<String, Class<? extends Fault>> classes = new HashMap<>();
		classes.put("fault-2", Fault2.class);
		Field classesField = Container.class.getDeclaredField("classes");
		classesField.setAccessible(true);
		classesField.set(container, classes);
		Fault fault = container.fault("fault-1", "fault-id-1", "{}");
		assertNull(fault);

		// Invalid JSON params specified - null returned
		fault = container.fault("fault-2", "fault-id-2", "{invalid : json}");
		assertNull(fault);

		// Other - fault with the specified id and params returned
		Fault2 fault2 = (Fault2) container.fault("fault-2", "fault-id-2", "{\"property2\":2,\"property3\":\"hello\"}");
		assertThat(fault2.id(), is("fault-id-2"));
		assertNull(fault2.getProperty1());
		assertThat(fault2.getProperty2(), is(2));
	}

	@Test
	public void testShutdown() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		// Sync executors shutdown
		Container container = new Container();
		Field repositorySyncExecutorField = Container.class.getDeclaredField("repositorySyncExecutor");
		repositorySyncExecutorField.setAccessible(true);
		ScheduledExecutorService repositorySyncExecutor = (ScheduledExecutorService) repositorySyncExecutorField.get(container);
		assertFalse(repositorySyncExecutor.isShutdown());
		Field schedulerSyncExecutorField = Container.class.getDeclaredField("schedulerSyncExecutor");
		schedulerSyncExecutorField.setAccessible(true);
		ScheduledExecutorService schedulerSyncExecutor = (ScheduledExecutorService) schedulerSyncExecutorField.get(container);
		assertFalse(schedulerSyncExecutor.isShutdown());
		container.shutdown();
		assertTrue(repositorySyncExecutor.isShutdown());
		assertTrue(schedulerSyncExecutor.isShutdown());
	}

}
