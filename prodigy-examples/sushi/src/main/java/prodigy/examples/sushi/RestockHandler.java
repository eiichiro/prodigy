package prodigy.examples.sushi;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class RestockHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
        Random random = new SecureRandom();
        Stream.of(Item.values()).forEach(i -> {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("name", new AttributeValue().withS(i.getName()));
            String update = "SET #stock = :stock";
            Map<String, String> names = new HashMap<>();
            names.put("#stock", "stock");
            Map<String, AttributeValue> values = new HashMap<>();
            values.put(":stock", new AttributeValue().withN(String.valueOf(random.nextInt(100))));
            UpdateItemRequest request = new UpdateItemRequest().withTableName("item").withKey(key)
                    .withUpdateExpression(update).withExpressionAttributeNames(names)
                    .withExpressionAttributeValues(values);
            dynamoDB.updateItem(request);
        });
        return new APIGatewayProxyResponseEvent().withStatusCode(200);
    }

}
