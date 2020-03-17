package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class ExitCommandTest {

	@Test
	public void testExitCommand() {}

	@Test
	public void testName() {
		ExitCommand command = new ExitCommand(new Shell());
		assertThat(command.name(), is("exit"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() {
		// TODO Not yet implemented
	}

}
