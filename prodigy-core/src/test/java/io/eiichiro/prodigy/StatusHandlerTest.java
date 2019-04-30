package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.junit.Test;

import io.eiichiro.prodigy.Scheduler.Entry;

public class StatusHandlerTest {

	@Test
	public void testHandleRequest() {
		// Scheduler throws Exception - APIGatewayProxyResponseEvent with status code 500
		StatusHandler handler = new StatusHandler();
		Scheduler scheduler = mock(Scheduler.class);
		doThrow(new IllegalStateException("hello")).when(scheduler).list();
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		APIGatewayProxyResponseEvent output = handler.handleRequest(new APIGatewayProxyRequestEvent(), null);
		assertThat(output.getStatusCode(), is(500));
		assertThat(output.getBody(), is("hello"));

		// 'id' is not specified - APIGatewayProxyResponseEvent with JSON list and status code 200
		scheduler = mock(Scheduler.class);
		List<Entry> entries = new ArrayList<>();
		Entry entry = new Entry("fault-id-1", "fault-1", "ACTIVE", "{}");
		entries.add(entry);
		doReturn(entries).when(scheduler).list();
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		output = handler.handleRequest(new APIGatewayProxyRequestEvent(), null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("[{\"id\":\"fault-id-1\",\"name\":\"fault-1\",\"status\":\"ACTIVE\",\"params\":\"{}\"}]"));

		// 'id' is specified and scheduler returns null - APIGatewayProxyResponseEvent with JSON object and status code 200
		scheduler = mock(Scheduler.class);
		doReturn(null).when(scheduler).get(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
		Map<String, String> queryStringParameters = new HashMap<>();
		queryStringParameters.put("id", "fault-id-2");
		input.setQueryStringParameters(queryStringParameters);
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertThat(output.getBody(), is("Fault id [fault-id-2] not found"));

		// 'id' is specified - APIGatewayProxyResponseEvent with JSON object and status code 200
		scheduler = mock(Scheduler.class);
		entry = new Entry("fault-id-2", "fault-2", "ACTIVE", "{}");
		doReturn(entry).when(scheduler).get(anyString());
		Prodigy.container(new Container(scheduler, mock(Repository.class)));
		input = new APIGatewayProxyRequestEvent();
		queryStringParameters = new HashMap<>();
		queryStringParameters.put("id", "fault-id-2");
		input.setQueryStringParameters(queryStringParameters);
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("{\"id\":\"fault-id-2\",\"name\":\"fault-2\",\"status\":\"ACTIVE\",\"params\":\"{}\"}"));
	}

}
