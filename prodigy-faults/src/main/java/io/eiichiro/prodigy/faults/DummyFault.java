package io.eiichiro.prodigy.faults;

import java.util.HashSet;
import java.util.Set;

import io.eiichiro.prodigy.Controller;
import io.eiichiro.prodigy.Fault;
import io.eiichiro.prodigy.Interceptor;
import io.eiichiro.prodigy.Invocation;
import io.eiichiro.prodigy.Named;
import io.eiichiro.prodigy.Validator;
import io.eiichiro.prodigy.Violation;

@Named("dummy")
public class DummyFault extends Fault implements Validator, Controller, Interceptor {

    @Override
    public void activate() {
        System.console().printf("dummy-%s is activated\n", id());
    }

    @Override
    public void deactivate() {
        System.console().printf("dummy-%s is deactivated\n", id());
    }

    @Override
    public Set<Violation> validate() {
        System.console().printf("dummy-%s is valid\n", id());
        return new HashSet<>();
    }

    @Override
    public boolean intercept(Invocation invocation) throws Throwable {
        System.console().printf("dummy-%s intercepts [%s]\n", id(), invocation);
        invocation.proceed();
        return true;
    }

}
