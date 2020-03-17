package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.HashMap;

import org.eiichiro.ash.Shell;
import org.junit.Test;

public class EjectCommandTest {

	@Test
	public void testEjectCommand() {}

	@Test
	public void testName() {
		EjectCommand command = new EjectCommand(new Shell(), new HashMap<>());
		assertThat(command.name(), is("eject"));
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
