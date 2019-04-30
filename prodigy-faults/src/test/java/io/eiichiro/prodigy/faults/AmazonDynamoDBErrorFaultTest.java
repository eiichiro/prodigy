package io.eiichiro.prodigy.faults;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.mock;

import java.util.Set;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;

import org.junit.Test;

import io.eiichiro.prodigy.Invocation;
import io.eiichiro.prodigy.Violation;

public class AmazonDynamoDBErrorFaultTest {

	@Test
	public void testValidate() {
		// 'statusCode' is null - 1 violation with the message starts with "Parameter
		// 'statusCode' is any one of ["
		AmazonDynamoDBErrorFault fault = new AmazonDynamoDBErrorFault();
		Set<Violation> violations = fault.validate();
		assertThat(violations.size(), is(1));
		assertTrue(violations.iterator().next().toString().startsWith("Parameter 'statusCode' is any one of ["));

		// 'statusCode' is 502 - 1 violation with the message starts with "Parameter
		// 'statusCode' is any one of ["
		fault = new AmazonDynamoDBErrorFault();
		fault.setStatusCode(502);
		violations = fault.validate();
		assertThat(violations.size(), is(1));
		assertTrue(violations.iterator().next().toString().startsWith("Parameter 'statusCode' is any one of ["));
		
		// 'statusCode' is 400 and 'errorCode' is null - 1 violation with the message
		// starts with "Parameter 'errorCode' is any one of ["
		fault = new AmazonDynamoDBErrorFault();
		fault.setStatusCode(400);
		violations = fault.validate();
		assertThat(violations.size(), is(1));
		assertTrue(violations.iterator().next().toString().startsWith("Parameter 'errorCode' is any one of ["));

		// 'statusCode' is 400 and 'errorCode' is "error-not-found" - 1 violation with
		// the message starts with "Parameter 'errorCode' is any one of ["
		fault = new AmazonDynamoDBErrorFault();
		fault.setStatusCode(400);
		fault.setErrorCode("error-not-found");
		violations = fault.validate();
		assertThat(violations.size(), is(1));
		assertTrue(violations.iterator().next().toString().startsWith("Parameter 'errorCode' is any one of ["));

		// 'statusCode' is 503 and 'errorCode' is null - no violation
		fault = new AmazonDynamoDBErrorFault();
		fault.setStatusCode(503);
		violations = fault.validate();
		assertTrue(violations.isEmpty());

		// 'statusCode' is 400 and 'errorCode' is "AccessDeniedException" - no violation
		fault = new AmazonDynamoDBErrorFault();
		fault.setStatusCode(400);
		fault.setErrorCode("AccessDeniedException");
		violations = fault.validate();
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testGetStatusCode() {}

	@Test
	public void testSetStatusCode() {}

	@Test
	public void testGetErrorCode() {}

	@Test
	public void testSetErrorCode() {}

	@Test
	public void testApply() throws Throwable {
		// Invocation target is not an instance of
		// com.amazonaws.services.dynamodbv2.AmazonDynamoDB - not proceeded and false
		// returned
		Invocation invocation = new Invocation(new Object(), Object.class.getMethod("toString"));
		AmazonDynamoDBErrorFault fault = new AmazonDynamoDBErrorFault();
		boolean applied = fault.apply(invocation);
		assertFalse(invocation.proceeded());
		assertFalse(applied);

		// Invocation target is an instance of AmazonDynamoDB, 'statusCode' is 503 and
		// 'errorCode' is null - not proceeded, AmazonDynamoDBException is with the 
		// error code of 'ServiceUnavailable' set and true returned
		invocation = new Invocation(mock(AmazonDynamoDB.class), AmazonDynamoDB.class.getMethod("getItem", GetItemRequest.class));
		fault = new AmazonDynamoDBErrorFault();
		fault.setStatusCode(503);
		applied = fault.apply(invocation);
		assertFalse(invocation.proceeded());
		assertThat(invocation.throwable(), instanceOf(AmazonDynamoDBException.class));
		AmazonDynamoDBException exception = (AmazonDynamoDBException) invocation.throwable();
		assertThat(exception.getStatusCode(), is(503));
		assertThat(exception.getErrorCode(), is("ServiceUnavailable"));
		assertThat(exception.getServiceName(), is("AmazonDynamoDBv2"));
		assertTrue(applied);

		// Invocation target is an instance of AmazonDynamoDB, 'statusCode' is 400 and
		// 'errorCode' is "AccessDeniedException" - not proceeded,
		// AmazonDynamoDBException is set and true returned
		invocation = new Invocation(mock(AmazonDynamoDB.class), AmazonDynamoDB.class.getMethod("getItem", GetItemRequest.class));
		fault = new AmazonDynamoDBErrorFault();
		fault.setStatusCode(400);
		fault.setErrorCode("AccessDeniedException");
		applied = fault.apply(invocation);
		assertFalse(invocation.proceeded());
		assertThat(invocation.throwable(), instanceOf(AmazonDynamoDBException.class));
		exception = (AmazonDynamoDBException) invocation.throwable();
		assertThat(exception.getStatusCode(), is(400));
		assertThat(exception.getErrorCode(), is("AccessDeniedException"));
		assertThat(exception.getServiceName(), is("AmazonDynamoDBv2"));
		assertTrue(applied);
	}

}
