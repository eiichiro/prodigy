package prodigy.examples.sushi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.eiichiro.prodigy.Prodigy;

public class OrderHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private AmazonDynamoDB dynamoDB = Prodigy.adapt(AmazonDynamoDBClientBuilder.defaultClient());

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> params;

        try {
            params = mapper.readValue(input.getBody(), new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonParseException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(e.getMessage());
        } catch (IOException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(e.getMessage());
        }

        String name = params.get("name").toString();
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("name", new AttributeValue().withS(name));
        String condition = "stock > :zero";
        String update = "SET #stock = #stock - :stock";
        Map<String, String> names = new HashMap<>();
        names.put("#stock", "stock");
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":stock", new AttributeValue().withN(String.valueOf(1)));
        values.put(":zero", new AttributeValue().withN(String.valueOf(0)));
        UpdateItemRequest request = new UpdateItemRequest().withTableName("item").withKey(key)
                .withConditionExpression(condition).withUpdateExpression(update).withExpressionAttributeNames(names)
                .withExpressionAttributeValues(values);

        try {
            dynamoDB.updateItem(request);
        } catch (ConditionalCheckFailedException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(name + " is out of stock");
        } catch (AmazonDynamoDBException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("HTTP " + e.getStatusCode() + " "
                    + e.getErrorCode() + " in " + e.getServiceName() + "; " + e.getMessage());
            // In-place fallback
            // name = "gizzard shad";
        }

        return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("{\"" + name + "\" : 1}");
    }

}