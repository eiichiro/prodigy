package prodigy.examples.sushi;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class OrderHandlerTest {

	@Test
	public void testHandleRequest() {
		// Invalid JSON - APIGatewayProxyResponseEvent with status code 400
		OrderHandler handler = new OrderHandler(mock(AmazonDynamoDB.class));
		APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
		input.setBody("invalid : json");
		APIGatewayProxyResponseEvent output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));

		// AmazonDynamoDB client throws ConditionalCheckFailedException - APIGatewayProxyResponseEvent with status code 400
		AmazonDynamoDB dynamoDB = mock(AmazonDynamoDB.class);
		doThrow(new ConditionalCheckFailedException("")).when(dynamoDB).updateItem(ArgumentMatchers.any(UpdateItemRequest.class));
		handler = new OrderHandler(dynamoDB);
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"name\" : \"salmon\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(400));
		assertThat(output.getBody(), is("salmon is out of stock"));
		
		// AmazonDynamoDB client throws AmazonDynamoDBException - APIGatewayProxyResponseEvent with status code 500
		dynamoDB = mock(AmazonDynamoDB.class);
		doThrow(new AmazonDynamoDBException("")).when(dynamoDB).updateItem(ArgumentMatchers.any(UpdateItemRequest.class));
		handler = new OrderHandler(dynamoDB);
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"name\" : \"salmon\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(500));

		// No exception - APIGatewayProxyResponseEvent with status code 200
		dynamoDB = mock(AmazonDynamoDB.class);
		doAnswer((i) -> {
			UpdateItemRequest request = i.getArgument(0);
			assertThat(request.getTableName(), is("item"));
			assertThat(request.getKey().get("name").getS(), is("salmon"));
			assertThat(request.getConditionExpression(), is("stock > :zero"));
			assertThat(request.getUpdateExpression(), is("SET #stock = #stock - :stock"));
			assertThat(request.getExpressionAttributeNames().get("#stock"), is("stock"));
			assertThat(request.getExpressionAttributeValues().get(":stock").getN(), is("1"));
			assertThat(request.getExpressionAttributeValues().get(":zero").getN(), is("0"));
			return null;
		}).when(dynamoDB).updateItem(ArgumentMatchers.any(UpdateItemRequest.class));
		handler = new OrderHandler(dynamoDB);
		input = new APIGatewayProxyRequestEvent();
		input.setBody("{\"name\" : \"salmon\"}");
		output = handler.handleRequest(input, null);
		assertThat(output.getStatusCode(), is(200));
		assertThat(output.getBody(), is("{\"salmon\" : 1}"));
	}

}
