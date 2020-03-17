package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class HintCommandTest {

	@Test
	public void testHintCommand() {}

	@Test
	public void testName() {
		HintCommand command = new HintCommand(new Shell());
		assertThat(command.name(), is("hint"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() {
		// TODO Not yet implemented
	}

}
