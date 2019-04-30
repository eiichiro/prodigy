package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class FaultsCommandTest {

	@Test
	public void testName() {
		FaultsCommand command = new FaultsCommand(new Shell(), new HashMap<>());
		assertThat(command.name(), is("faults"));
	}

	@Test
	public void testUsage() {
		FaultsCommand command = new FaultsCommand(new Shell(), new HashMap<>());
		Usage usage = command.usage();
		assertThat(usage.synopsis(), is("faults"));
		assertThat(usage.options().size(), is(0));
	}

	@Test
	public void testRun() throws Exception {
		Map<String, Object> configuration = new HashMap<>();
		configuration.put("default", "profile-1");
		Map<String, String> profile1 = new HashMap<>();
		profile1.put("endpoint", "https://endpoint-1.execute-api.us-east-1.amazonaws.com/beta");
		configuration.put("profile-1", profile1);
		Map<String, String> profile2 = new HashMap<>();
		profile2.put("endpoint", "https://endpoint-2.execute-api.us-east-1.amazonaws.com/beta");
		configuration.put("profile-2", profile2);
		FaultsCommand command = new FaultsCommand(new Shell(), configuration);
		CloseableHttpResponse response = mock(CloseableHttpResponse.class);
		StatusLine line = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "");
		doReturn(line).when(response).getStatusLine();
		HttpEntity entity = new StringEntity("{}", ContentType.APPLICATION_JSON);
		doReturn(entity).when(response).getEntity();
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		doAnswer((i) -> {
			HttpGet get = i.getArgument(0);
			assertThat(get.getURI().toString(), is("https://endpoint-1.execute-api.us-east-1.amazonaws.com/beta/faults"));
			return response;
		}).when(httpClient).execute(ArgumentMatchers.any(HttpUriRequest.class));
		command.httpClient(httpClient);
		Map<String, String> options = new HashMap<>();
		List<String> args = new ArrayList<>();
		command.run(new Line("faults", options, args));

		entity = new StringEntity("{\"fault-2\" : \"io.eiichiro.prodigy.Fault2\", \"fault-1\" : \"io.eiichiro.prodigy.Fault1\"}", ContentType.APPLICATION_JSON);
		doReturn(entity).when(response).getEntity();
		command.run(new Line("faults", options, args));

		line = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "");
		doReturn(line).when(response).getStatusLine();
		command.run(new Line("faults", options, args));
	}

}
