package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.nio.file.Paths;
import java.util.HashMap;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class DeployCommandTest {

	@Test
	public void testDeployCommand() {}

	@Test
	public void testName() {
		DeployCommand command = new DeployCommand(new Shell(), new HashMap<>(), Paths.get("/path/to/not-found.yml"));
		assertThat(command.name(), is("deploy"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() {
		// TODO Not yet implemented
	}

	@Test
	public void testCloudFormation() {}

	@Test
	public void testS3() {}

}
