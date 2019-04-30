package io.eiichiro.prodigy;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProvisionedConcurrencyTest {

	@Test
	public void testWarmup() {
		Prodigy.configuration(null);
		Prodigy.container(null);
		ProvisionedConcurrency.warmup();
		assertNotNull(Prodigy.configuration());
		assertNotNull(Prodigy.container());
	}

}
