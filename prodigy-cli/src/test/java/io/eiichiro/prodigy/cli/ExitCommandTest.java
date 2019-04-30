package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.*;

import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;
import org.junit.Test;

public class ExitCommandTest {

	@Test
	public void testName() {
		ExitCommand command = new ExitCommand(new Shell());
		assertThat(command.name(), is("exit"));
	}

	@Test
	public void testUsage() {
		ExitCommand command = new ExitCommand(new Shell());
		Usage usage = command.usage();
		assertThat(usage.synopsis(), is("exit"));
		assertThat(usage.options().size(), is(0));
	}

	@Test
	public void testRun() throws Exception {
		Shell shell = new Shell();
		ExitCommand command = new ExitCommand(shell);
		Field field = Shell.class.getDeclaredField("repl");
		field.setAccessible(true);
		AtomicBoolean repl = (AtomicBoolean) field.get(shell);
		repl.set(true);
		assertTrue(shell.started());
		command.run(new Line("exit"));
		assertFalse(shell.started());
	}

}
