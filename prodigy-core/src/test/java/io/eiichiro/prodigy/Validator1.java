package io.eiichiro.prodigy;

import java.util.HashSet;
import java.util.Set;

public class Validator1 extends Fault implements Validator {

    @Override
    public Set<Violation> validate() {
        Set<Violation> violations = new HashSet<>();
        violations.add(new Violation("message"));
        return violations;
    }

}