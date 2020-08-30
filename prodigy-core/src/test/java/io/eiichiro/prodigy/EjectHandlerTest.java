package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.junit.Test;

public class EjectHandlerTest {

	@Test
	public void testHandleRequest() {
		// Invalid JSON - APIGatewayProxyResponseEvent with status code 400
		EjectHandler handler = new EjectHandler();
		APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
		input.setBody("invalid : json");
		APIGatewayProxyResponseEvent output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertTrue(output.getBody().startsWith("Parameter must be a JSON object: "));

		// Prodigy throws IllegalArgumentException - APIGatewayProxyResponseEvent with status code 400
		Scheduler scheduler = mock(Scheduler.class);
		doReturn(false).when(scheduler).unschedule(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"id\" : \"fault-id-1\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertThat(output.getBody(), is("Fault id [fault-id-1] not found"));

		// Prodigy throws Exception - APIGatewayProxyResponseEvent with status code 500
		scheduler = mock(Scheduler.class);
		doThrow(new IllegalStateException("hello")).when(scheduler).unschedule(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"id\" : \"fault-id-1\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(500));
		assertThat(output.getBody(), is("hello"));

		// No exception - APIGatewayProxyResponseEvent with status code 200
		scheduler = mock(Scheduler.class);
		doReturn(true).when(scheduler).unschedule(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"id\" : \"fault-id-2\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("{}"));
	}

}
