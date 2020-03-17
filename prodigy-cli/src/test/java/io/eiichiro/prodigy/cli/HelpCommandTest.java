package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class HelpCommandTest {

	@Test
	public void testHelpCommand() {}

	@Test
	public void testName() {
		HelpCommand command = new HelpCommand(new Shell());
		assertThat(command.name(), is("help"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() {
		// TODO Not yet implemented
	}

}
