package io.eiichiro.prodigy.faults;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import io.eiichiro.prodigy.Controller;
import io.eiichiro.prodigy.Fault;
import io.eiichiro.prodigy.Interceptor;
import io.eiichiro.prodigy.LambdaLogFactory;
import io.eiichiro.prodigy.Named;
import io.eiichiro.prodigy.Validator;
import io.eiichiro.prodigy.Violation;

@Named("dummy")
public class DummyFault extends Fault implements Validator, Controller, Interceptor {

    private Log log = LambdaLogFactory.getLog(getClass());

    @Override
    public void activate() {
        log.info("");
    }

    @Override
    public void deactivate() {
        log.info("");
    }

    @Override
    public Set<Violation> validate() {
        log.info("");
        return new HashSet<>();
    }

}
