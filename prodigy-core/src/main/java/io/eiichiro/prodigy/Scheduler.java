/*
 * Copyright (C) 2019-2020 Eiichiro Uchiumi and The Prodigy Authors. All 
 * Rights Reserved.
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
package io.eiichiro.prodigy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eiichiro.reverb.lang.UncheckedException;

public class Scheduler {

    private static final int EXPIRE = 60 * 60 * 24;

    private final Log log = LogFactory.getLog(getClass());

    private final AmazonDynamoDB dynamoDB;

    public Scheduler() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public Scheduler(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public boolean schedule(Fault fault) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue().withS(fault.id()));
        Class<? extends Fault> clazz = fault.getClass();
        Named named = clazz.getAnnotation(Named.class);
        item.put("name", new AttributeValue().withS((named != null) ? named.value() : clazz.getSimpleName()));
        item.put("status", new AttributeValue().withS("ACTIVE"));
        ObjectMapper mapper = new ObjectMapper();

        try {
            item.put("params", new AttributeValue().withS(mapper.writeValueAsString(fault)));
        } catch (JsonProcessingException e) {
            throw new UncheckedException(e);
        }

        item.put("ttl", new AttributeValue().withN(String.valueOf(Instant.now().getEpochSecond() + EXPIRE)));
        PutItemRequest request = new PutItemRequest().withTableName(Prodigy.configuration().scheduler()).withItem(item)
                .withConditionExpression("attribute_not_exists(id)");
        
        try {
                dynamoDB.putItem(request);
                return true;
            } catch (ConditionalCheckFailedException e) {
                log.warn(e.getMessage(), e);
                return false;
            }
        }

    public boolean unschedule(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue().withS(id));
        String condition = "attribute_exists(id)";
        String update = "SET #ttl = :ttl, #status = :status";
        Map<String, String> names = new HashMap<>();
        names.put("#ttl", "ttl");
        names.put("#status", "status");
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":ttl", new AttributeValue().withN(String.valueOf(Instant.now().getEpochSecond())));
        values.put(":status", new AttributeValue().withS("INACTIVE"));
        UpdateItemRequest request = new UpdateItemRequest().withTableName(Prodigy.configuration().scheduler())
                .withKey(key).withConditionExpression(condition).withUpdateExpression(update)
                .withExpressionAttributeNames(names).withExpressionAttributeValues(values);
        
        try {
            dynamoDB.updateItem(request);
            return true;
        } catch (ConditionalCheckFailedException e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public Entry get(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue().withS(id));
        GetItemResult result = dynamoDB.getItem(Prodigy.configuration().scheduler(), key);
        Map<String, AttributeValue> item = result.getItem();

        if (item == null || item.isEmpty()) {
            return null;
        }

        Entry entry = new Entry(item.get("id").getS(), item.get("name").getS(), item.get("status").getS(),
                item.get("params").getS());
        return entry;
    }

    public List<Entry> list() {
        List<String> attributes = new ArrayList<>();
        attributes.add("id");
        attributes.add("name");
        attributes.add("status");
        attributes.add("params");
        ScanResult result = dynamoDB.scan(Prodigy.configuration().scheduler(), attributes);
        List<Entry> entries = new ArrayList<>();
        result.getItems().forEach(i -> entries.add(
                new Entry(i.get("id").getS(), i.get("name").getS(), i.get("status").getS(), i.get("params").getS())));
        return entries;
    }

    static class Entry {

        private final String id;

        private final String name;

        private final String status;

        private final String params;

        Entry(String id, String name, String status, String params) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.params = params;
        }

        /**
         * @return the params
         */
        public String getParams() {
            return params;
        }

        /**
         * @return the status
         */
        public String getStatus() {
            return status;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

    }

}
