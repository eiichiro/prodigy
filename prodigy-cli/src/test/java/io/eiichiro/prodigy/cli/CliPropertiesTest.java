package io.eiichiro.prodigy.cli;

import static org.junit.Assert.*;

import org.junit.Test;

public class CliPropertiesTest {

	@Test
	public void testLoad() {
		CliProperties properties = new CliProperties();
		properties.load();
		assertTrue(properties.containsKey("version.prodigy.cli"));
		assertTrue(properties.containsKey("dependency.prodigy.core"));
		assertTrue(properties.containsKey("dependency.prodigy.faults"));
	}

	@Test
	public void testVersion() {
		CliProperties properties = new CliProperties();
		properties.load();
		assertNotNull(properties.version("prodigy.cli"));
	}

	@Test
	public void testDependency() {
		CliProperties properties = new CliProperties();
		properties.load();
		assertNotNull(properties.dependency("prodigy.core"));
		assertNotNull(properties.dependency("prodigy.faults"));
	}

}
