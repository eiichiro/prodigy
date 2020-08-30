package io.eiichiro.prodigy;

import static org.junit.Assert.*;

import org.junit.Test;

public class HandlersTest {

	@Test
	public void testWarmup() {
		Prodigy.configuration(null);
		Prodigy.container(null);
		Handlers.warmup();
		assertNotNull(Prodigy.configuration());
		assertNotNull(Prodigy.container());
	}

}
