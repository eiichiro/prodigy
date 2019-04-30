package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;
import org.junit.Test;

public class HintCommandTest {

	@Test
	public void testName() {
		HintCommand command = new HintCommand(new Shell());
		assertThat(command.name(), is("hint"));
	}

	@Test
	public void testUsage() {
		HintCommand command = new HintCommand(new Shell());
		Usage usage = command.usage();
		assertThat(usage.synopsis(), is("hint"));
		assertThat(usage.options().size(), is(0));
	}

	@Test
	public void testRun() throws Exception {
		Shell shell = new Shell();
		shell.register(new ExitCommand(shell));
		shell.register(new HelpCommand(shell));
		HintCommand command = new HintCommand(shell);
		command.run(new Line("hint"));
	}

}
