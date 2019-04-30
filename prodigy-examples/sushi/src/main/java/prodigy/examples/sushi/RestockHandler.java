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

/**
 * {@code RestockHandler} handles <code>/restock</code> request.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public class RestockHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private AmazonDynamoDB dynamoDB;

    public RestockHandler() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    // For testing
    public RestockHandler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    /**
     * Handles <code>/restock</code> request and sets the stock for every 
     * {@code Item} with random number.
     */
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
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
