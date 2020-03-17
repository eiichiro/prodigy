package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class PushCommandTest {

	@Test
	public void testPushCommand() {}

	@Test
	public void testName() {
		PushCommand command = new PushCommand(new Shell(), new HashMap<>());
		assertThat(command.name(), is("push"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() {
		// TODO Not yet implemented
	}

	@Test
	public void testHttpClient() {}

}
