package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.eiichiro.reverb.system.Environment;
import org.junit.Test;

public class ConfigurationTest {

	@Test
	public void testConfiguration() {
		Configuration configuration = new Configuration();
		assertNull(configuration.repository());
		assertNull(configuration.scheduler());

		Environment.setenv(Configuration.PRODIGY_FAULT_REPOSITORY, "repository-1");
		Environment.setenv(Configuration.PRODIGY_FAULT_SCHEDULER, "scheduler-2");
		configuration = new Configuration();
		assertThat(configuration.repository(), is("repository-1"));
		assertThat(configuration.scheduler(), is("scheduler-2"));
	}

	@Test
	public void testConfigurationStringString() {
		Configuration configuration = new Configuration("scheduler-3", "repository-4");
		assertThat(configuration.repository(), is("repository-4"));
		assertThat(configuration.scheduler(), is("scheduler-3"));
	}

}
