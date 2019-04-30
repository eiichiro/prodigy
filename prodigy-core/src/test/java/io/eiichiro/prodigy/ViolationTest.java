package io.eiichiro.prodigy;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

public class ViolationTest {

	@Test
	public void testToString() {
		Violation violation = new Violation("message");
		assertThat(violation.toString(), is("message"));
	}

}
