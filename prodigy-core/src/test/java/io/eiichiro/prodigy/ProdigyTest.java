package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import io.eiichiro.prodigy.Scheduler.Entry;

public class ProdigyTest {

	@Test
	public void testAdapt() {
		Interface1 interface1 = Prodigy.adapt(new Impl1());
		Interface2 interface2 = (Interface2) interface1;
		interface2.toString();	// To suppress warning

		try {
			Impl1 impl1 = Prodigy.adapt(new Impl1());
			impl1.toString();	// To suppress warning
			fail();
		} catch (ClassCastException e) {}
	}

	@Test
	public void testInject() {
		// Parameter 'name' is not specified - IllegalArgumentException
		try {
			Prodigy.inject(null, null);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Parameter 'name' is required"));
		}

		// Container returns null - IllegalArgumentException
		Container container = mock(Container.class);
		doReturn(null).when(container).fault(anyString(), anyString());
		Prodigy.container(container);

		try {
			Prodigy.inject("fault-1", null);
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Fault cannot be instantiated with name [fault-1] and params [{}]"));
		}

		// Validator returns violations - IllegalArgumentException
		container = mock(Container.class);
		doReturn(new Validator1()).when(container).fault(anyString(), anyString());
		Prodigy.container(container);

		try {
			Prodigy.inject("fault-1", null);
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Parameter 'params' is invalid: [message]"));
		}

		// Scheduler returns false - IllegalArgumentException
		Scheduler scheduler = mock(Scheduler.class);
		doReturn(false).when(scheduler).schedule(ArgumentMatchers.any(Fault.class));
		container = mock(Container.class);
		Fault fault = new Validator2();
		fault.id("fault-id-2");
		doReturn(fault).when(container).fault(anyString(), anyString());
		doReturn(scheduler).when(container).scheduler();
		Prodigy.container(container);

		try {
			Prodigy.inject("fault-2", null);
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Fault id [fault-id-2] already exists"));
		}

		// Parameter 'params' is not specified
		scheduler = mock(Scheduler.class);
		doReturn(true).when(scheduler).schedule(ArgumentMatchers.any(Fault.class));
		container = mock(Container.class);
		fault = new Validator2();
		fault.id("fault-id-2");
		Fault result = fault;
		doAnswer(i -> {
			assertThat(i.getArgument(1), is("{}"));
			return result;
		}).when(container).fault(anyString(), anyString());
		doReturn(scheduler).when(container).scheduler();
		Prodigy.container(container);
		String id = Prodigy.inject("fault-2", null);
		assertThat(id, is("fault-id-2"));

		// Parameter 'params' is specified
		scheduler = mock(Scheduler.class);
		doReturn(true).when(scheduler).schedule(ArgumentMatchers.any(Fault.class));
		container = mock(Container.class);
		fault = new Validator2();
		fault.id("fault-id-2");
		doReturn(fault).when(container).fault(anyString(), anyString());
		doReturn(scheduler).when(container).scheduler();
		Prodigy.container(container);
		id = Prodigy.inject("fault-2", "{\"key-1\" : \"value-1\"}");
		assertThat(id, is("fault-id-2"));
	}

	@Test
	public void testEject() {
		// Parameter 'id' is not specified - IllegalArgumentException
		try {
			Prodigy.eject(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Parameter 'id' is required"));
		}

		// Scheduler returns false - IllegalArgumentException
		Scheduler scheduler = mock(Scheduler.class);
		doReturn(false).when(scheduler).unschedule(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));

		try {
			Prodigy.eject("fault-id-1");
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Fault id [fault-id-1] not found"));
		}

		// No exception
		scheduler = mock(Scheduler.class);
		doReturn(true).when(scheduler).unschedule(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		Prodigy.eject("fault-id-2");
	}

	@Test
	public void testFaults() {
		// No exception
		Repository repository = mock(Repository.class);
		Map<String, Class<? extends Fault>> faults = new LinkedHashMap<>();
		faults.put("fault-1", Fault1.class);
		faults.put("fault-2", Fault2.class);
		doReturn(faults).when(repository).load();
		Prodigy.container(new Container(mock(Scheduler.class), repository));
		assertSame(faults, Prodigy.faults());
	}

	@Test
	public void testStatus() {
		// Parameter 'id' is not specified - IllegalArgumentException
		try {
			Prodigy.status(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Parameter 'id' is required"));
		}

		// 'id' is specified and corresponding entry not found - IllegalArgumentException
		Scheduler scheduler = mock(Scheduler.class);
		doReturn(null).when(scheduler).get(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));

		try {
			Prodigy.status("fault-id-2");
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Fault id [fault-id-2] not found"));
		}

		// 'id' is specified and corresponding entry found
		scheduler = mock(Scheduler.class);
		Entry entry = new Entry("fault-id-2", "fault-2", "ACTIVE", "{}");
		doReturn(entry).when(scheduler).get(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		assertSame(entry, Prodigy.status("fault-id-2"));

		// 'id' is not specified
		scheduler = mock(Scheduler.class);
		List<Entry> entries = new ArrayList<>();
		entry = new Entry("fault-id-1", "fault-1", "ACTIVE", "{}");
		entries.add(entry);
		doReturn(entries).when(scheduler).list();
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		assertSame(entries, Prodigy.status());
	}

	@Test
	public void testPush() {
		// Parameter 'name' is not specified - IllegalArgumentException
		try {
			Prodigy.push(null, null);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Parameter 'name' is required"));
		}

		// Parameter 'name' does not end with '.jar' extension - IllegalArgumentException
		try {
			Prodigy.push("push-handler-test.zip", null);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(e.getMessage(), is("Parameter 'name' must be *.jar"));
		}

		// Other
		Repository repository = mock(Repository.class);
		doNothing().when(repository).save(anyString(), ArgumentMatchers.any(InputStream.class));
		Prodigy.container(new Container(mock(Scheduler.class), repository));
		Prodigy.push("push-handler-test.jar", null);
	}

}
