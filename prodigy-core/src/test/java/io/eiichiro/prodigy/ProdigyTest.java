package io.eiichiro.prodigy;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProdigyTest {

	@Test
	public void testAdapt() {
		Interface1 interface1 = Prodigy.adapt(new Impl1());
		Interface2 interface2 = (Interface2) interface1;
		interface2.toString();	// To suppress warning

		try {
			Impl1 impl1 = Prodigy.adapt(new Impl1());
			impl1.toString();	// To suppress warning
			fail();
		} catch (ClassCastException e) {}
	}

}
