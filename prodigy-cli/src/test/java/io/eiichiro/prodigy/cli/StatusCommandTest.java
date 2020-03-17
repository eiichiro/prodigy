package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class StatusCommandTest {

	@Test
	public void testStatusCommand() {}

	@Test
	public void testName() {
		StatusCommand command = new StatusCommand(new Shell(), new HashMap<>());
		assertThat(command.name(), is("status"));
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
