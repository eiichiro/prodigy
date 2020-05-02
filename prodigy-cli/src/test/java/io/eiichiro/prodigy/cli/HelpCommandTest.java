package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;

import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;
import org.junit.Test;

public class HelpCommandTest {

	@Test
	public void testName() {
		HelpCommand command = new HelpCommand(new Shell());
		assertThat(command.name(), is("help"));
	}

	@Test
	public void testUsage() {
		HelpCommand command = new HelpCommand(new Shell());
		Usage usage = command.usage();
		assertThat(usage.synopsis(), is("help <command>"));
		assertThat(usage.options().size(), is(0));
	}

	@Test
	public void testRun() throws Exception {
		Shell shell = new Shell();
		HelpCommand command = new HelpCommand(shell);
		Map<String, String> options = new HashMap<>();
		List<String> args = new ArrayList<>();
		command.run(new Line("help", options, args));

		args = new ArrayList<>();
		args.add("not-found");
		command.run(new Line("help", options, args));

		shell.register(new ExitCommand(shell));
		args = new ArrayList<>();
		args.add("exit");
		command.run(new Line("help", options, args));
	}

}
