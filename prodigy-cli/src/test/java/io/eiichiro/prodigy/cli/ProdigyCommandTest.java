package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.junit.Test;

public class ProdigyCommandTest {

	@Test
	public void testName() {
		ProdigyCommand command = new ProdigyCommand(new Shell());
		assertThat(command.name(), is("prodigy"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() throws Exception {
		ProdigyCommand command = new ProdigyCommand(new Shell());
		Line line = new Line("prodigy", Arrays.asList("one liner"));
		command.run(line);
		Log log = LogFactory.getLog(getClass());
		assertFalse(log.isDebugEnabled());

		command = new ProdigyCommand(new Shell());
		Map<String, String> map = new HashMap<>();
		map.put("v", "");
		line = new Line("prodigy", map, Arrays.asList("one liner"));
		LogManager.shutdown();
		command.run(line);
		log = LogFactory.getLog(getClass());
		assertTrue(log.isDebugEnabled());

		command = new ProdigyCommand(new Shell());
		map.clear();
		map.put("verbose", "");
		line = new Line("prodigy", map, Arrays.asList("one liner"));
		LogManager.shutdown();
		command.run(line);
		log = LogFactory.getLog(getClass());
		assertTrue(log.isDebugEnabled());

		command = new ProdigyCommand(new Shell());
		map.clear();
		map.put("config", "/path/to/not-found.yml");
		line = new Line("prodigy", map, Arrays.asList("one liner"));
		command.run(line);
		Shell shell = command.shell();
		Map<String, Command> commands = shell.commands();
		assertThat(commands.size(), is(1));
		assertThat(commands.get(new DeployCommand(null, null, null).name()), instanceOf(DeployCommand.class));

		command = new ProdigyCommand(new Shell());
		map.clear();
		map.put("config", getClass().getResource("/prodigy-command-test.yml").getPath());
		line = new Line("prodigy", map, Arrays.asList("one liner"));
		command.run(line);
		shell = command.shell();
		commands = shell.commands();
		assertThat(commands.size(), is(7));
		assertThat(commands.get(new DeployCommand(null, null, null).name()), instanceOf(DeployCommand.class));
		assertThat(commands.get(new InjectCommand(null, null).name()), instanceOf(InjectCommand.class));
		assertThat(commands.get(new EjectCommand(null, null).name()), instanceOf(EjectCommand.class));
		assertThat(commands.get(new ConfigureCommand(null, null, null).name()), instanceOf(ConfigureCommand.class));
		assertThat(commands.get(new StatusCommand(null, null).name()), instanceOf(StatusCommand.class));
		assertThat(commands.get(new FaultsCommand(null, null).name()), instanceOf(FaultsCommand.class));
		assertThat(commands.get(new PushCommand(null, null).name()), instanceOf(PushCommand.class));

		command = new ProdigyCommand(spyShell());
		map.clear();
		map.put("config", "/path/to/not-found.yml");
		command.run(new Line("prodigy", map, new ArrayList<>()));
		shell = command.shell();
		verify(shell).start();
		assertThat(shell.console().prompt(), is("prodigy> "));
		commands = shell.commands();
		assertThat(commands.size(), is(4));
		assertThat(commands.get(new DeployCommand(null, null, null).name()), instanceOf(DeployCommand.class));
		assertThat(commands.get(new HintCommand(null).name()), instanceOf(HintCommand.class));
		assertThat(commands.get(new HelpCommand(null).name()), instanceOf(HelpCommand.class));
		assertThat(commands.get(new ExitCommand(null).name()), instanceOf(ExitCommand.class));

		command = new ProdigyCommand(spyShell());
		map.clear();
		map.put("config", getClass().getResource("/prodigy-command-test.yml").getPath());
		command.run(new Line("prodigy", map, new ArrayList<>()));
		shell = command.shell();
		verify(shell).start();
		assertThat(shell.console().prompt(), is("prodigy|profile-1> "));
		commands = shell.commands();
		assertThat(commands.size(), is(10));
		assertThat(commands.get(new DeployCommand(null, null, null).name()), instanceOf(DeployCommand.class));
		assertThat(commands.get(new InjectCommand(null, null).name()), instanceOf(InjectCommand.class));
		assertThat(commands.get(new EjectCommand(null, null).name()), instanceOf(EjectCommand.class));
		assertThat(commands.get(new ConfigureCommand(null, null, null).name()), instanceOf(ConfigureCommand.class));
		assertThat(commands.get(new StatusCommand(null, null).name()), instanceOf(StatusCommand.class));
		assertThat(commands.get(new FaultsCommand(null, null).name()), instanceOf(FaultsCommand.class));
		assertThat(commands.get(new PushCommand(null, null).name()), instanceOf(PushCommand.class));
		assertThat(commands.get(new HintCommand(null).name()), instanceOf(HintCommand.class));
		assertThat(commands.get(new HelpCommand(null).name()), instanceOf(HelpCommand.class));
		assertThat(commands.get(new ExitCommand(null).name()), instanceOf(ExitCommand.class));
	}

	private Shell spyShell() {
		Shell shell = spy(Shell.class);
		doNothing().when(shell).start();
		return shell;
	}

}
