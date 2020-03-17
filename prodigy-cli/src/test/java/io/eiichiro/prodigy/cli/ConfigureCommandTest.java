package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.nio.file.Paths;
import java.util.HashMap;

import org.eiichiro.ash.Shell;

import org.junit.Test;

public class ConfigureCommandTest {

	@Test
	public void testConfigureCommand() {}

	@Test
	public void testName() {
		ConfigureCommand command = new ConfigureCommand(new Shell(), new HashMap<>(), Paths.get("/path/to/not-found.yml"));
		assertThat(command.name(), is("configure"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() {
		// TODO Not yet implemented
	}

}
