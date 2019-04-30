/*
 * Copyright (C) 2019-present Eiichiro Uchiumi and the Prodigy Authors. 
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * {@code DummyFault} is a fault implementation that does nothing but logging.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
@Named("dummy")
public class DummyFault extends Fault implements Validator, Controller, Interceptor {

    /**
     * Controls nothing but logging.
     */
    @Override
    public void activate() {
        System.out.printf("dummy-%s is activated\n", id());
    }

    /**
     * Controls nothing but logging.
     */
    @Override
    public void deactivate() {
        System.out.printf("dummy-%s is deactivated\n", id());
    }

    /**
     * Validates nothing but logging.
     */
    @Override
    public Set<Violation> validate() {
        System.out.printf("dummy-%s is valid\n", id());
        return new HashSet<>();
    }

    /**
     * Intercepts all method invocations and proceeds after the logging.
     */
    @Override
    public boolean apply(Invocation invocation) throws Throwable {
        System.out.printf("dummy-%s intercepts [%s]\n", id(), invocation);
        invocation.proceed();
        return true;
    }

}
