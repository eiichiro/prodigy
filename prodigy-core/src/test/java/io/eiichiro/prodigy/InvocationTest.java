package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

public class InvocationTest {

	@Test
	public void testProceed() throws Throwable {
		Invocation invocation = new Invocation(new Target1(), Target1.class.getMethod("method1", String.class));
		invocation.args(new Object[] {"hello"});
		invocation.proceed();
		assertThat(invocation.result(), is("hello"));
		assertTrue(invocation.proceeded());

		invocation = new Invocation(new Target1(), Target1.class.getMethod("method2", Exception.class));
		invocation.args(new Object[] {new RuntimeException("goodbye")});
		invocation.proceed();
		assertThat(invocation.throwable(), instanceOf(RuntimeException.class));
		assertThat(invocation.throwable().getMessage(), is("goodbye"));
		assertNull(invocation.result());

		try {
			invocation.proceed();
			fail();
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), is("This invocation has already proceeded"));
		}
	}

}
