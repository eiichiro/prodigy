package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class FaultsCommandTest {

	@Test
	public void testFaultsCommand() {}

	@Test
	public void testName() {
		FaultsCommand command = new FaultsCommand(new Shell(), new HashMap<>());
		assertThat(command.name(), is("faults"));
	}

	@Test
	public void testUsage() {}

	@Test
	public void testRun() {
		// TODO Not yet implemented
	}

	@Test
	public void testHttpClient() {}

}
