package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.junit.Test;

public class FaultsHandlerTest {

	@Test
	public void testHandleRequest() {
		// Repository throws Exception - APIGatewayProxyResponseEvent with status code 500
		FaultsHandler handler = new FaultsHandler();
		Repository repository = mock(Repository.class);
		doThrow(new IllegalStateException("hello")).when(repository).load();
		Prodigy.container(new Container(mock(Scheduler.class), repository));
		APIGatewayProxyResponseEvent output = handler.handleRequest(null, null);
		assertThat(output.getStatusCode(), is(500));
		assertThat(output.getBody(), is("hello"));

		// No exception - APIGatewayProxyResponseEvent with status code 200
		repository = mock(Repository.class);
		Map<String, Class<? extends Fault>> faults = new LinkedHashMap<>();
		faults.put("fault-1", Fault1.class);
		faults.put("fault-2", Fault2.class);
		doReturn(faults).when(repository).load();
		Prodigy.container(new Container(mock(Scheduler.class), repository));
		output = handler.handleRequest(null, null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("{\"fault-1\":\"io.eiichiro.prodigy.Fault1\",\"fault-2\":\"io.eiichiro.prodigy.Fault2\"}"));
	}

}
