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

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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

public class EjectCommandTest {

	@Test
	public void testName() {
		EjectCommand command = new EjectCommand(new Shell(), new HashMap<>());
		assertThat(command.name(), is("eject"));
	}

	@Test
	public void testUsage() {
		EjectCommand command = new EjectCommand(new Shell(), new HashMap<>());
		Usage usage = command.usage();
		assertThat(usage.synopsis(), is("eject <fault-id>"));
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
		EjectCommand command = new EjectCommand(new Shell(), configuration);
		CloseableHttpResponse response = mock(CloseableHttpResponse.class);
		StatusLine line = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "");
		doReturn(line).when(response).getStatusLine();
		HttpEntity entity = new StringEntity("{}", ContentType.APPLICATION_JSON);
		doReturn(entity).when(response).getEntity();
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		doAnswer((i) -> {
			HttpPost post = i.getArgument(0);
			assertThat(post.getURI().toString(), is("https://endpoint-1.execute-api.us-east-1.amazonaws.com/beta/eject"));
			assertThat(post.getEntity().getContentType().getValue(), is(ContentType.APPLICATION_JSON.toString()));
			assertThat(IOUtils.toString(post.getEntity().getContent(), ContentType.APPLICATION_JSON.getCharset()), is("{\"id\":\"fault-id-1\"}"));
			return response;
		}).when(httpClient).execute(ArgumentMatchers.any(HttpUriRequest.class));
		command.httpClient(httpClient);
		Map<String, String> options = new HashMap<>();
		List<String> args = new ArrayList<>();
		args.add("fault-id-1");
		command.run(new Line("eject", options, args));

		line = new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "");
		doReturn(line).when(response).getStatusLine();
		command.run(new Line("eject", options, args));

		command = new EjectCommand(new Shell(), configuration);
		args = new ArrayList<>();
		command.run(new Line("eject", options, args));

		command = new EjectCommand(new Shell(), configuration);
		args = new ArrayList<>();
		args.add("fault-id-1");
		args.add("fault-id-2");
		command.run(new Line("eject", options, args));
	}

}
