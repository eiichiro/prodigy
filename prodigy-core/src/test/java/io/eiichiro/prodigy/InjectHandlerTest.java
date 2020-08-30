package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.hamcrest.CoreMatchers.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class InjectHandlerTest {

	@Test
	public void testHandleRequest() {
		// Invalid JSON - APIGatewayProxyResponseEvent with status code 400
		InjectHandler handler = new InjectHandler();
		APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
		handler = new InjectHandler();
		input = new APIGatewayProxyRequestEvent();
		input.setBody("invalid : json");
		APIGatewayProxyResponseEvent output = handler.handleRequest(input, null);
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertTrue(output.getBody().startsWith("Parameter must be a JSON object: "));

		// Prodigy throws IllegalArgumentException - APIGatewayProxyResponseEvent with status code 400
		Container container = mock(Container.class);
		doReturn(null).when(container).fault(anyString(), anyString());
		Prodigy.container(container);
		handler = new InjectHandler();
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"name\" : \"fault-1\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertThat(output.getBody(), is("Fault cannot be instantiated with name [fault-1] and params [{}]"));

		// Prodigy throws Exception - APIGatewayProxyResponseEvent with status code 500
		Scheduler scheduler = mock(Scheduler.class);
		doThrow(new IllegalStateException("hello")).when(scheduler).schedule(ArgumentMatchers.any(Fault.class));
		container = mock(Container.class);
		doReturn(new Validator2()).when(container).fault(anyString(), anyString());
		doReturn(scheduler).when(container).scheduler();
		Prodigy.container(container);
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"name\" : \"fault-2\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(500));
		assertThat(output.getBody(), is("hello"));

		// Parameter 'params' is not specified - APIGatewayProxyResponseEvent with status code 200
		scheduler = mock(Scheduler.class);
		doReturn(true).when(scheduler).schedule(ArgumentMatchers.any(Fault.class));
		container = mock(Container.class);
		Fault fault = new Validator2();
		fault.id("fault-id-2");
		Fault result = fault;
		doAnswer(i -> {
			assertThat(i.getArgument(1), is("{}"));
			return result;
		}).when(container).fault(anyString(), anyString());
		doReturn(scheduler).when(container).scheduler();
		Prodigy.container(container);
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"name\" : \"fault-2\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("{\"id\":\"fault-id-2\"}"));

		// Parameter 'params' is specified - APIGatewayProxyResponseEvent with status code 200
		scheduler = mock(Scheduler.class);
		doReturn(true).when(scheduler).schedule(ArgumentMatchers.any(Fault.class));
		container = mock(Container.class);
		fault = new Validator2();
		fault.id("fault-id-2");
		doReturn(fault).when(container).fault(anyString(), anyString());
		doReturn(scheduler).when(container).scheduler();
		Prodigy.container(container);
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"name\" : \"fault-2\", \"params\" : \"{\\\"key-1\\\" : \\\"value-1\\\"}\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("{\"id\":\"fault-id-2\"}"));
	}

}
