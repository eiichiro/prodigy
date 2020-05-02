package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class ClientInvocationHandlerTest {

	@Test
	public void testInvoke() throws NoSuchMethodException, SecurityException, Throwable {
		// Interceptor sets invocation result and returns true as the return value - invocation result returned and subsequent interceptor not invoked
		Container container = mock(Container.class);
		Map<String, Interceptor> interceptors = new LinkedHashMap<>();
		interceptors.put("interceptor-1", new Interceptor1().result("result-1").applies(true));
		interceptors.put("interceptor-2", new Interceptor1().result("result-2").applies(true));
		doReturn(interceptors).when(container).interceptors();
		Prodigy.container(container);
		ClientInvocationHandler handler = new ClientInvocationHandler(new Object());
		Object result = handler.invoke(null, Object.class.getMethod("toString"), null);
		assertThat(result, is("result-1"));
		assertTrue(((Interceptor1) interceptors.get("interceptor-1")).invoked());
		assertFalse(((Interceptor1) interceptors.get("interceptor-2")).invoked());

		// Interceptor sets exception and returns true as the return value - exception thrown and subsequent interceptor not invoked
		container = mock(Container.class);
		interceptors = new LinkedHashMap<>();
		interceptors.put("interceptor-3", new Interceptor1().throwable(new IllegalStateException()).applies(true));
		interceptors.put("interceptor-4", new Interceptor1().throwable(new IllegalArgumentException()).applies(true));
		doReturn(interceptors).when(container).interceptors();
		Prodigy.container(container);
		handler = new ClientInvocationHandler(new Object());

		try {
			handler.invoke(null, Object.class.getMethod("toString"), null);
			fail();
		} catch (IllegalStateException e) {}

		assertTrue(((Interceptor1) interceptors.get("interceptor-3")).invoked());
		assertFalse(((Interceptor1) interceptors.get("interceptor-4")).invoked());

		// All interceptors return false and 'proceed' method not invoked on the invocation - target object invoked
		container = mock(Container.class);
		interceptors = new LinkedHashMap<>();
		interceptors.put("interceptor-5", new Interceptor1().result("result-5").applies(false));
		interceptors.put("interceptor-6", new Interceptor1().result("result-6").applies(false));
		doReturn(interceptors).when(container).interceptors();
		Prodigy.container(container);
		handler = new ClientInvocationHandler("hello");
		result = handler.invoke(null, String.class.getMethod("toString"), null);
		assertThat(result, is("hello"));
		assertTrue(((Interceptor1) interceptors.get("interceptor-5")).invoked());
		assertTrue(((Interceptor1) interceptors.get("interceptor-6")).invoked());

		// All interceptors return false, 'proceed' method invoked on the invocation and exception set - exception thrown
		container = mock(Container.class);
		interceptors = new LinkedHashMap<>();
		interceptors.put("interceptor-7", new Interceptor1().throwable(new IllegalStateException()).applies(false).proceeds(true));
		interceptors.put("interceptor-8", new Interceptor1().throwable(new IllegalArgumentException()).applies(false));
		doReturn(interceptors).when(container).interceptors();
		Prodigy.container(container);
		handler = new ClientInvocationHandler(new Object());

		try {
			handler.invoke(null, Object.class.getMethod("toString"), null);
			fail();
		} catch (IllegalArgumentException e) {}

		assertTrue(((Interceptor1) interceptors.get("interceptor-7")).invoked());
		assertTrue(((Interceptor1) interceptors.get("interceptor-8")).invoked());

		// All interceptors return false, 'proceed' method invoked on the invocation and exception not set - invocation result returned
		container = mock(Container.class);
		interceptors = new LinkedHashMap<>();
		interceptors.put("interceptor-9", new Interceptor1().result("result-9").applies(false).proceeds(true));
		interceptors.put("interceptor-10", new Interceptor1().result("result-10").applies(false));
		doReturn(interceptors).when(container).interceptors();
		Prodigy.container(container);
		handler = new ClientInvocationHandler(new Object());
		result = handler.invoke(null, Object.class.getMethod("toString"), null);
		assertThat(result, is("result-10"));
		assertTrue(((Interceptor1) interceptors.get("interceptor-9")).invoked());
		assertTrue(((Interceptor1) interceptors.get("interceptor-10")).invoked());
	}

}
