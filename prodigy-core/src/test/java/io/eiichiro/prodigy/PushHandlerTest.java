package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class PushHandlerTest {

	@Test
	public void testHandleRequest() throws UnsupportedOperationException, IOException {
		// No 'jar' field - APIGatewayProxyResponseEvent with status code 400
		APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
		HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("not-jar", Paths.get(getClass().getResource("/push-handler-test.jar").getPath()).toFile()).build();
		Map<String, String> headers = new HashMap<>();
		Header header = entity.getContentType();
		headers.put(header.getName(), header.getValue());
		// API Gateway automatically encodes raw binary bytes as Base64 string.
		input.setBody(Base64.getEncoder().encodeToString(IOUtils.toString(entity.getContent()).getBytes()));
		input.setHeaders(headers);
		PushHandler handler = new PushHandler();
		APIGatewayProxyResponseEvent output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertThat(output.getBody(), is("Parameter 'jar' is required"));

		// File name does not end with '.jar' extension - APIGatewayProxyResponseEvent with status code 400
		input = new APIGatewayProxyRequestEvent();
		entity = MultipartEntityBuilder.create().addBinaryBody("jar", Paths.get(getClass().getResource("/push-handler-test.zip").getPath()).toFile()).build();
		headers = new HashMap<>();
		header = entity.getContentType();
		headers.put(header.getName(), header.getValue());
		input.setBody(Base64.getEncoder().encodeToString(IOUtils.toString(entity.getContent()).getBytes()));
		input.setHeaders(headers);
		handler = new PushHandler();
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertThat(output.getBody(), is("Parameter 'jar' must be a jar file"));

		// Repository throws Exception - APIGatewayProxyResponseEvent with status code 500
		input = new APIGatewayProxyRequestEvent();
		entity = MultipartEntityBuilder.create().addBinaryBody("jar", Paths.get(getClass().getResource("/push-handler-test.jar").getPath()).toFile()).build();
		headers = new HashMap<>();
		header = entity.getContentType();
		headers.put(header.getName(), header.getValue());
		input.setBody(Base64.getEncoder().encodeToString(IOUtils.toString(entity.getContent()).getBytes()));
		input.setHeaders(headers);
		handler = new PushHandler();
		Repository repository = mock(Repository.class);
		doThrow(new IllegalStateException("hello")).when(repository).save(anyString(), ArgumentMatchers.any(InputStream.class));
		Prodigy.container(new Container(mock(Scheduler.class), repository));
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(500));
		assertThat(output.getBody(), is("hello"));

		// Other - APIGatewayProxyResponseEvent with JSON object and status code 200
		input = new APIGatewayProxyRequestEvent();
		entity = MultipartEntityBuilder.create().addBinaryBody("jar", Paths.get(getClass().getResource("/push-handler-test.jar").getPath()).toFile()).build();
		headers = new HashMap<>();
		header = entity.getContentType();
		headers.put(header.getName(), header.getValue());
		input.setBody(Base64.getEncoder().encodeToString(IOUtils.toString(entity.getContent()).getBytes()));
		input.setHeaders(headers);
		handler = new PushHandler();
		repository = mock(Repository.class);
		doNothing().when(repository).save(anyString(), ArgumentMatchers.any(InputStream.class));
		Prodigy.container(new Container(mock(Scheduler.class), repository));
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("{}"));
	}

}
