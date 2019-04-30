/*
 * Copyright (C) 2019-present Eiichiro Uchiumi and the Prodigy Authors. 
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * {@code OrderHandler} handles <code>/order</code> request.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public class OrderHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private AmazonDynamoDB dynamoDB;
    
    // Manipulates AmazonDynamoDB to adapt to Prodigy.
    public OrderHandler() {
        this(Prodigy.adapt(AmazonDynamoDBClientBuilder.defaultClient()));
    }

    // For testing
    public OrderHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    /**
     * Handles <code>/order</code> request and decrements the stock of the 
     * specified item. If request processing fails for any reason, it responds 
     * with the following error.
     * <table>
     *  <tr>
     *      <th>Status code</th><th>Reason</th>
     *  </tr>
     *  <tr>
     *      <td>400</td><td>Failed to parse input JSON</td>
     *  </tr>
     *  <tr>
     *      <td>400</td><td>Specified item is out of stock</td>
     *  </tr>
     *  <tr>
     *      <td>500</td><td>Failed to read request</td>
     *  </tr>
     *  <tr>
     *      <td>500</td><td>Downstream Amazon DynamoDB throws any unexpected exception</td>
     *  </tr>
     * </table>
     */
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> params;

        try {
            params = mapper.readValue(input.getBody(), new TypeReference<Map<String, String>>() {});
        } catch (JsonParseException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(e.getMessage());
        } catch (IOException e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(e.getMessage());
        }

        String name = params.get("name");
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
            // 'dynamodb-error' fault affects here.
            // In-place fallback
            // name = "gizzard shad";
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("HTTP " + e.getStatusCode() + " "
                    + e.getErrorCode() + " in " + e.getServiceName() + "; " + e.getMessage());
        }

        return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("{\"" + name + "\" : 1}");
    }

}