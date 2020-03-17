package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class InjectCommandTest {

	@Test
	public void testInjectCommand() {}

	@Test
	public void testName() {
		InjectCommand command = new InjectCommand(new Shell(), new HashMap<>());
		assertThat(command.name(), is("inject"));
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
