package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.hamcrest.CoreMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.net.ssl.*", "javax.management.*"})
@PrepareForTest(DeployCommand.class)
public class DeployCommandTest {

	@Test
	public void testName() {
		DeployCommand command = new DeployCommand(new Shell(), new HashMap<>(), Paths.get("/path/to/not-found.yml"));
		assertThat(command.name(), is("deploy"));
	}

	@Test
	public void testUsage() {
		DeployCommand command = new DeployCommand(new Shell(), new HashMap<>(), Paths.get("/path/to/not-found.yml"));
		Usage usage = command.usage();
		assertThat(usage.synopsis(), is("deploy <profile> ('deploy core' is not allowed)"));
		assertThat(usage.options().size(), is(0));
	}

	@Test
	public void testRun() throws Exception {
		// TODO 'private Map<String, String> deploy(String profile)' method behavior testing
		// Args size is 1 and the specified profile is 'core' - unsupported usage.
		Map<String, Object> configuration = new HashMap<>();
		configuration.put("default", "profile-1");
		Map<String, String> profile1 = new HashMap<>();
		profile1.put("endpoint", "https://endpoint-1.execute-api.us-east-1.amazonaws.com/beta");
		configuration.put("profile-1", profile1);
		Map<String, String> profile2 = new HashMap<>();
		profile2.put("endpoint", "https://endpoint-2.execute-api.us-east-1.amazonaws.com/beta");
		configuration.put("profile-2", profile2);
		DeployCommand command = new DeployCommand(new Shell(), configuration, Paths.get(getClass().getResource("/deploy-command-test.yml").getPath()));
		Map<String, String> options = new HashMap<>();
		List<String> args = new ArrayList<>();
		args.add("core");
		command.run(new Line("deploy", options, args));
		assertThat(configuration.size(), is(3));
		assertThat(configuration.get("default"), is("profile-1"));

		// Args size is 1, the specified profile is not 'core' and configuration file does not exist - configuration is updated with the specified profile and ConfigureCommand, InjectCommand and EjectCommand are registered.
		Shell shell = new Shell();
		command = spy(new DeployCommand(shell, configuration, Paths.get("/path/to/not-found.yml")));
		Map<String, String> c = new HashMap<>();
		c.put("endpoint", "https://endpoint-3.execute-api.us-east-1.amazonaws.com/beta");
		doReturn(c).when(command, "deploy", anyString());
		doNothing().when(command, "touch");
		doNothing().when(command, "write");
		doReturn(new ArrayList<>()).when(command, "read");
		args = new ArrayList<>();
		args.add("profile-3");
		command.run(new Line("deploy", options, args));
		assertThat(configuration.size(), is(4));
		assertThat(configuration.get("default"), is("profile-3"));
		assertThat(shell.commands().size(), is(6));
		assertThat(shell.commands().get(new ConfigureCommand(null, null, null).name()), instanceOf(ConfigureCommand.class));
		assertThat(shell.commands().get(new InjectCommand(null, null).name()), instanceOf(InjectCommand.class));
		assertThat(shell.commands().get(new EjectCommand(null, null).name()), instanceOf(EjectCommand.class));
		assertThat(shell.commands().get(new StatusCommand(null, null).name()), instanceOf(StatusCommand.class));
		assertThat(shell.commands().get(new FaultsCommand(null, null).name()), instanceOf(FaultsCommand.class));
		assertThat(shell.commands().get(new PushCommand(null, null).name()), instanceOf(PushCommand.class));
		verifyPrivate(command, times(1)).invoke("deploy", anyString());
		verifyPrivate(command, times(1)).invoke("touch");
		verifyPrivate(command, times(1)).invoke("write");

		// Args size is 1, the specified profile is not 'core' and configuration file exists - configuration is updated with the specified profile and ConfigureCommand, InjectCommand and EjectCommand are registered.
		shell = new Shell();
		command = spy(new DeployCommand(shell, configuration, Paths.get(getClass().getResource("/deploy-command-test.yml").getPath())));
		c = new HashMap<>();
		c.put("endpoint", "https://endpoint-4.execute-api.us-east-1.amazonaws.com/beta");
		doReturn(c).when(command, "deploy", anyString());
		doNothing().when(command, "touch");
		doNothing().when(command, "write");
		args = new ArrayList<>();
		args.add("profile-4");
		command.run(new Line("deploy", options, args));
		assertThat(configuration.size(), is(5));
		assertThat(configuration.get("default"), is("profile-4"));
		assertThat(shell.commands().size(), is(6));
		assertThat(shell.commands().get(new ConfigureCommand(null, null, null).name()), instanceOf(ConfigureCommand.class));
		assertThat(shell.commands().get(new InjectCommand(null, null).name()), instanceOf(InjectCommand.class));
		assertThat(shell.commands().get(new EjectCommand(null, null).name()), instanceOf(EjectCommand.class));
		assertThat(shell.commands().get(new StatusCommand(null, null).name()), instanceOf(StatusCommand.class));
		assertThat(shell.commands().get(new FaultsCommand(null, null).name()), instanceOf(FaultsCommand.class));
		assertThat(shell.commands().get(new PushCommand(null, null).name()), instanceOf(PushCommand.class));
		verifyPrivate(command, times(1)).invoke("deploy", anyString());
		verifyPrivate(command, never()).invoke("touch");
		verifyPrivate(command, times(1)).invoke("write");

		// Any other cases - unsupported usage.
		// Args size is not 1
		command = new DeployCommand(new Shell(), configuration, Paths.get(getClass().getResource("/deploy-command-test.yml").getPath()));
		args = new ArrayList<>();
		command.run(new Line("deploy", options, args));
		assertThat(configuration.size(), is(5));
		assertThat(configuration.get("default"), is("profile-4"));

		command = new DeployCommand(new Shell(), configuration, Paths.get(getClass().getResource("/deploy-command-test.yml").getPath()));
		args = new ArrayList<>();
		args.add("profile-1");
		args.add("profile-2");
		command.run(new Line("deploy", options, args));
		assertThat(configuration.size(), is(5));
		assertThat(configuration.get("default"), is("profile-4"));
	}

}
