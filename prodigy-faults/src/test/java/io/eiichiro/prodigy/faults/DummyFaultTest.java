package io.eiichiro.prodigy.faults;

import static org.junit.Assert.*;

import org.junit.Test;

import io.eiichiro.prodigy.Invocation;

public class DummyFaultTest {

	@Test
	public void testActivate() {
		new DummyFault().activate();
	}

	@Test
	public void testDeactivate() {
		new DummyFault().deactivate();
	}

	@Test
	public void testValidate() {
		assertTrue(new DummyFault().validate().isEmpty());
	}

	@Test
	public void testApply() throws Throwable {
		Invocation invocation = new Invocation(new Object(), Object.class.getMethod("toString"));
		new DummyFault().apply(invocation);
		assertTrue(invocation.proceeded());
	}

}
