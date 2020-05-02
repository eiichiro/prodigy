package io.eiichiro.prodigy;

import java.util.HashSet;
import java.util.Set;

public class Validator2 extends Fault implements Validator {

    @Override
    public Set<Violation> validate() {
        return new HashSet<>();
    }

}