package prodigy.examples.sushi;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class RestockHandlerTest {

	@Test
	public void testHandleRequest() {
		AmazonDynamoDB dynamoDB = mock(AmazonDynamoDB.class);
		doAnswer((i) -> {
			UpdateItemRequest request = i.getArgument(0);
			assertThat(request.getTableName(), is("item"));
			assertThat(request.getUpdateExpression(), is("SET #stock = :stock"));
			assertThat(request.getExpressionAttributeNames().get("#stock"), is("stock"));
			int stock = Integer.valueOf(request.getExpressionAttributeValues().get(":stock").getN());
			assertTrue(0 <= stock && stock < 100);
			return null;
		}).when(dynamoDB).updateItem(ArgumentMatchers.any(UpdateItemRequest.class));
		RestockHandler handler = new RestockHandler(dynamoDB);
		APIGatewayProxyResponseEvent output = handler.handleRequest(null, null);
		verify(dynamoDB, times(Item.values().length)).updateItem(ArgumentMatchers.any(UpdateItemRequest.class));
		assertThat(output.getStatusCode(), is(200));
	}

}
