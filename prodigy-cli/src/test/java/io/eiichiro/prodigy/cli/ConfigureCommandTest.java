package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
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
@PowerMockIgnore("org.apache.logging.log4j.*")
@PrepareForTest(ConfigureCommand.class)
public class ConfigureCommandTest {

	@Test
	public void testConfigureCommand() {}

	@Test
	public void testName() {
		ConfigureCommand command = new ConfigureCommand(new Shell(), new HashMap<>(), Paths.get("/path/to/not-found.yml"));
		assertThat(command.name(), is("configure"));
	}

	@Test
	public void testUsage() {
		ConfigureCommand command = new ConfigureCommand(new Shell(), new HashMap<>(), Paths.get("/path/to/not-found.yml"));
		Usage usage = command.usage();
		assertThat(usage.synopsis(), is("configure [options] <profile>"));
		assertThat(usage.options().size(), is(1));
	}

	@Test
	public void testRun() throws Exception {
		// Args size is 0 and option not specified - existing configuration printed (but not verified) and not updated.
		Map<String, Object> configuration = new HashMap<>();
		configuration.put("default", "profile-1");
		Map<String, String> profile1 = new HashMap<>();
		profile1.put("endpoint", "https://endpoint-1.execute-api.us-east-1.amazonaws.com/beta");
		configuration.put("profile-1", profile1);
		Map<String, String> profile2 = new HashMap<>();
		profile2.put("endpoint", "https://endpoint-2.execute-api.us-east-1.amazonaws.com/beta");
		configuration.put("profile-2", profile2);
		ConfigureCommand command = new ConfigureCommand(new Shell(), configuration, Paths.get(getClass().getResource("/configure-command-test.yml").getPath()));
		command.run(new Line("configure", new HashMap<>(), new ArrayList<>()));
		assertThat(configuration.size(), is(3));
		assertThat(configuration.get("default"), is("profile-1"));
		
		// Args size is 1, '--default' option specified and configuration does not contain the specified profile - warning printed (not verified) and configuration not updated.
		command = new ConfigureCommand(new Shell(), configuration, Paths.get(getClass().getResource("/configure-command-test.yml").getPath()));
		Map<String, String> options = new HashMap<>();
		options.put("default", null);
		List<String> args = new ArrayList<>();
		args.add("not-found");
		command.run(new Line("configure", options, args));
		assertThat(configuration.size(), is(3));
		assertThat(configuration.get("default"), is("profile-1"));

		// Args size is 1, '--default' option specified and configuration contains the specified profile - 'default' key in the configuration is updated with the specified profile.
		command = spy(new ConfigureCommand(new Shell(), configuration, Paths.get(getClass().getResource("/configure-command-test.yml").getPath())));
		doNothing().when(command, "write");
		args = new ArrayList<>();
		args.add("profile-2");
		command.run(new Line("configure", options, args));
		assertThat(configuration.size(), is(3));
		assertThat(configuration.get("default"), is("profile-2"));
		verifyPrivate(command, times(1)).invoke("write");

		// Any other cases - unsupported usage and configuration not updated.
		// Args size is 0 and option specified
		configuration = new HashMap<>();
		configuration.put("default", "profile-1");
		options = new HashMap<>();
		options.put("default", null);
		args = new ArrayList<>();
		command.run(new Line("configure", options, args));
		assertThat(configuration.size(), is(1));
		assertThat(configuration.get("default"), is("profile-1"));

		// Args size is 1 and not '--default' option specified
		options = new HashMap<>();
		args = new ArrayList<>();
		args.add("profile-2");
		command.run(new Line("configure", options, args));
		assertThat(configuration.size(), is(1));
		assertThat(configuration.get("default"), is("profile-1"));

		// Args size is greater than 1
		args = new ArrayList<>();
		args.add("profile-2");
		args.add("profile-3");
		command.run(new Line("configure", options, args));
		assertThat(configuration.size(), is(1));
		assertThat(configuration.get("default"), is("profile-1"));
	}

}
