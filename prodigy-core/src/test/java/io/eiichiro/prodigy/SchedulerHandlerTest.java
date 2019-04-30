package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.OperationType;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

import org.junit.Test;

public class SchedulerHandlerTest {

	@Test
	public void testHandleRequest() {
		// OperationType.INSERT and status is ACTIVE - 'activate()' invoked
		DynamodbEvent input = new DynamodbEvent();
		List<DynamodbStreamRecord> records = new ArrayList<>();
		DynamodbStreamRecord record = new DynamodbStreamRecord();
		StreamRecord r = new StreamRecord();
		Map<String, AttributeValue> image = new HashMap<>();
		image.put("status", new AttributeValue("ACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setNewImage(image);
		record.setEventName(OperationType.INSERT);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		Container container = mock(Container.class);
		Controller1 controller1 = new Controller1();
		doReturn(controller1).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		SchedulerHandler handler = new SchedulerHandler();
		handler.handleRequest(input, null);
		assertTrue(controller1.activated);

		// OperationType.INSERT and status is not ACTIVE - 'activate()' not invoked
		input = new DynamodbEvent();
		records = new ArrayList<>();
		record = new DynamodbStreamRecord();
		r = new StreamRecord();
		image = new HashMap<>();
		image.put("status", new AttributeValue("INACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setNewImage(image);
		record.setEventName(OperationType.INSERT);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		container = mock(Container.class);
		controller1 = new Controller1();
		doReturn(controller1).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		handler = new SchedulerHandler();
		handler.handleRequest(input, null);
		assertFalse(controller1.activated);

		// OperationType.MODIFY and status is INACTIVE - 'deactivate()' invoked
		input = new DynamodbEvent();
		records = new ArrayList<>();
		record = new DynamodbStreamRecord();
		r = new StreamRecord();
		image = new HashMap<>();
		image.put("status", new AttributeValue("INACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setNewImage(image);
		record.setEventName(OperationType.MODIFY);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		container = mock(Container.class);
		controller1 = new Controller1();
		doReturn(controller1).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		handler = new SchedulerHandler();
		handler.handleRequest(input, null);
		assertTrue(controller1.deactivated);

		// OperationType.MODIFY and status is not INACTIVE - 'deactivate()' not invoked
		input = new DynamodbEvent();
		records = new ArrayList<>();
		record = new DynamodbStreamRecord();
		r = new StreamRecord();
		image = new HashMap<>();
		image.put("status", new AttributeValue("ACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setNewImage(image);
		record.setEventName(OperationType.MODIFY);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		container = mock(Container.class);
		controller1 = new Controller1();
		doReturn(controller1).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		handler = new SchedulerHandler();
		handler.handleRequest(input, null);
		assertFalse(controller1.deactivated);

		// OperationType.REMOVE and status is ACTIVE - 'deactivate()' invoked
		input = new DynamodbEvent();
		records = new ArrayList<>();
		record = new DynamodbStreamRecord();
		r = new StreamRecord();
		image = new HashMap<>();
		image.put("status", new AttributeValue("ACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setOldImage(image);
		record.setEventName(OperationType.REMOVE);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		container = mock(Container.class);
		controller1 = new Controller1();
		doReturn(controller1).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		handler = new SchedulerHandler();
		handler.handleRequest(input, null);
		assertTrue(controller1.deactivated);

		// OperationType.REMOVE and status is not ACTIVE - 'deactivate()' not invoked
		input = new DynamodbEvent();
		records = new ArrayList<>();
		record = new DynamodbStreamRecord();
		r = new StreamRecord();
		image = new HashMap<>();
		image.put("status", new AttributeValue("INACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setOldImage(image);
		record.setEventName(OperationType.REMOVE);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		container = mock(Container.class);
		controller1 = new Controller1();
		doReturn(controller1).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		handler = new SchedulerHandler();
		handler.handleRequest(input, null);
		assertFalse(controller1.deactivated);

		// Controller throws exception - the exception thrown again
		input = new DynamodbEvent();
		records = new ArrayList<>();
		record = new DynamodbStreamRecord();
		r = new StreamRecord();
		image = new HashMap<>();
		image.put("status", new AttributeValue("ACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setNewImage(image);
		record.setEventName(OperationType.INSERT);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		container = mock(Container.class);
		doReturn(new Controller2()).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		handler = new SchedulerHandler();

		try {
			handler.handleRequest(input, null);
			fail();
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), is("hello"));
		}

		input = new DynamodbEvent();
		records = new ArrayList<>();
		record = new DynamodbStreamRecord();
		r = new StreamRecord();
		image = new HashMap<>();
		image.put("status", new AttributeValue("INACTIVE"));
		image.put("name", new AttributeValue("fault-1"));
		image.put("id", new AttributeValue("fault-id-1"));
		image.put("params", new AttributeValue("{}"));
		r.setNewImage(image);
		record.setEventName(OperationType.MODIFY);
		record.setDynamodb(r);
		records.add(record);
		input.setRecords(records);
		container = mock(Container.class);
		doReturn(new Controller2()).when(container).fault(anyString(), anyString(), anyString());
		Prodigy.container(container);
		handler = new SchedulerHandler();
		
		try {
			handler.handleRequest(input, null);
			fail();
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), is("hello"));
		}
	}

}
