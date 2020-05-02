package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

import org.junit.Test;

import io.eiichiro.prodigy.Scheduler.Entry;

public class SchedulerTest {

	@Test
	public void testSchedule() {
		// AmazonDynamoDB returns ConditionalCheckFailedException - false returned
		AmazonDynamoDB dynamoDB = mock(AmazonDynamoDB.class);
		doThrow(new ConditionalCheckFailedException("hello")).when(dynamoDB).putItem(any(PutItemRequest.class));
		Scheduler scheduler = new Scheduler(dynamoDB);
		assertFalse(scheduler.schedule(new Fault2()));

		// 'putItem()' on AmazonDynamoDB succeeds - true returned
		dynamoDB = mock(AmazonDynamoDB.class);
		doAnswer(i -> {
			PutItemRequest request = i.getArgument(0);
			Map<String, AttributeValue> item = request.getItem();
			assertThat(item.size(), is(5));
			assertThat(item.get("id").getS(), is("fault-id-2"));
			assertThat(item.get("name").getS(), is("Fault2"));
			assertThat(item.get("status").getS(), is("ACTIVE"));
			assertThat(item.get("params").getS(), is("{\"property1\":\"property2\",\"property2\":2}"));
			assertThat(request.getConditionExpression(), is("attribute_not_exists(id)"));
			return null;
		}).when(dynamoDB).putItem(any(PutItemRequest.class));
		scheduler = new Scheduler(dynamoDB);
		Fault2 fault2 = new Fault2();
		fault2.id("fault-id-2");
		fault2.setProperty1("property2");
		fault2.setProperty2(2);
		assertTrue(scheduler.schedule(fault2));
		
		dynamoDB = mock(AmazonDynamoDB.class);
		doAnswer(i -> {
			PutItemRequest request = i.getArgument(0);
			Map<String, AttributeValue> item = request.getItem();
			assertThat(item.size(), is(5));
			assertThat(item.get("id").getS(), is("fault-id-4"));
			assertThat(item.get("name").getS(), is("fault-4"));
			assertThat(item.get("status").getS(), is("ACTIVE"));
			assertThat(item.get("params").getS(), is("{\"property1\":\"property4\",\"property2\":4}"));
			assertThat(request.getConditionExpression(), is("attribute_not_exists(id)"));
			return null;
		}).when(dynamoDB).putItem(any(PutItemRequest.class));
		scheduler = new Scheduler(dynamoDB);
		Fault4 fault4 = new Fault4();
		fault4.id("fault-id-4");
		fault4.setProperty1("property4");
		fault4.setProperty2(4);
		assertTrue(scheduler.schedule(fault4));
	}

	@Test
	public void testUnschedule() {
		// AmazonDynamoDB returns ConditionalCheckFailedException - false returned
		AmazonDynamoDB dynamoDB = mock(AmazonDynamoDB.class);
		doThrow(new ConditionalCheckFailedException("hello")).when(dynamoDB).updateItem(any(UpdateItemRequest.class));
		Scheduler scheduler = new Scheduler(dynamoDB);
		assertFalse(scheduler.unschedule("fault-id-1"));

		// 'updateItem()' on AmazonDynamoDB succeeds - true returned
		dynamoDB = mock(AmazonDynamoDB.class);
		doAnswer(i -> {
			UpdateItemRequest request = i.getArgument(0);
			assertThat(request.getKey().get("id").getS(), is("fault-id-2"));
			assertThat(request.getConditionExpression(), is("attribute_exists(id)"));
			assertThat(request.getUpdateExpression(), is("SET #ttl = :ttl, #status = :status"));
			Map<String, String> names = request.getExpressionAttributeNames();
			assertThat(names.size(), is(2));
			assertThat(names.get("#ttl"), is("ttl"));
			assertThat(names.get("#status"), is("status"));
			Map<String, AttributeValue> values = request.getExpressionAttributeValues();
			assertThat(values.size(), is(2));
			assertThat(values.get(":status").getS(), is("INACTIVE"));
			assertTrue(Long.valueOf(values.get(":ttl").getN()) <= Instant.now().getEpochSecond());
			return null;
		}).when(dynamoDB).updateItem(any(UpdateItemRequest.class));
		scheduler = new Scheduler(dynamoDB);
		assertTrue(scheduler.unschedule("fault-id-2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGet() {
		// Item not found - null returned
		Configuration configuration = new Configuration("", "");
		Prodigy.configuration(configuration);
		AmazonDynamoDB dynamoDB = mock(AmazonDynamoDB.class);
		GetItemResult result = new GetItemResult();
		result.setItem(null);
		doReturn(result).when(dynamoDB).getItem(anyString(), any(Map.class));
		Scheduler scheduler = new Scheduler(dynamoDB);
		Entry entry = scheduler.get("fault-id-1");
		assertNull(entry);

		dynamoDB = mock(AmazonDynamoDB.class);
		result = new GetItemResult();
		result.setItem(new HashMap<>());
		doReturn(result).when(dynamoDB).getItem(anyString(), any(Map.class));
		scheduler = new Scheduler(dynamoDB);
		entry = scheduler.get("fault-id-1");
		assertNull(entry);

		// Item found - entry returned
		dynamoDB = mock(AmazonDynamoDB.class);
		result = new GetItemResult();
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", new AttributeValue("fault-id-2"));
		item.put("name", new AttributeValue("fault-2"));
		item.put("status", new AttributeValue("ACTIVE"));
		item.put("params", new AttributeValue("{}"));
		result.setItem(item);
		doReturn(result).when(dynamoDB).getItem(anyString(), any(Map.class));
		scheduler = new Scheduler(dynamoDB);
		entry = scheduler.get("fault-id-2");
		assertThat(entry.getId(), is("fault-id-2"));
		assertThat(entry.getName(), is("fault-2"));
		assertThat(entry.getStatus(), is("ACTIVE"));
		assertThat(entry.getParams(), is("{}"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testList() {
		// Items not found - empty list returned
		Configuration configuration = new Configuration("", "");
		Prodigy.configuration(configuration);
		AmazonDynamoDB dynamoDB = mock(AmazonDynamoDB.class);
		ScanResult result = new ScanResult();
		result.setItems(new ArrayList<>());
		doReturn(result).when(dynamoDB).scan(anyString(), any(List.class));
		Scheduler scheduler = new Scheduler(dynamoDB);
		List<Entry> entries = scheduler.list();
		assertTrue(entries.isEmpty());

		// Items found - entry list returned
		dynamoDB = mock(AmazonDynamoDB.class);
		result = new ScanResult();
		List<Map<String, AttributeValue>> items = new ArrayList<>();
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("id", new AttributeValue("fault-id-3"));
		item.put("name", new AttributeValue("fault-3"));
		item.put("status", new AttributeValue("INACTIVE"));
		item.put("params", new AttributeValue("{}"));
		items.add(item);
		result.setItems(items);
		doReturn(result).when(dynamoDB).scan(anyString(), any(List.class));
		scheduler = new Scheduler(dynamoDB);
		entries = scheduler.list();
		assertThat(entries.size(), is(1));
		Entry entry = entries.get(0);
		assertThat(entry.getId(), is("fault-id-3"));
		assertThat(entry.getName(), is("fault-3"));
		assertThat(entry.getStatus(), is("INACTIVE"));
		assertThat(entry.getParams(), is("{}"));
	}

}
