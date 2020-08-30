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
package io.eiichiro.prodigy;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.eiichiro.prodigy.Scheduler.Entry;

/**
 * {@code Prodigy} is the helper class to adapt application objects to Prodigy 
 * and give an access to core runtime components internally.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public final class Prodigy {

    private static Log log = LogFactory.getLog(Prodigy.class);

    private static Configuration configuration;

    private static Container container;

    private Prodigy() {}

    static Configuration configuration() {
        if (configuration == null) {
            synchronized (Prodigy.class) {
                if (configuration == null) {
                    configuration = new Configuration();
                }
            }
        }

        return configuration;
    }

    static void configuration(Configuration configuration) {
        Prodigy.configuration = configuration;
    }

    static Container container() {
        if (container == null) {
            synchronized (Prodigy.class) {
                if (container == null) {
                    container = new Container();
                }
            }
        }

        return container;
    }

    static void container(Container container) {
        Prodigy.container = container;
    }

    /**
     * Manipulates the specified the target object to adapt to Prodigy.
     * Method invocation on the Prodigy-adapted object is intercepted by the 
     * faults implement {@code Interceptor}. Specified target object must be 
     * an interface type.
     * 
     * @param <T> Any interface type.
     * @param target The target object to adapt to Prodigy.
     * @return Prodigy-adapted target object.
     * @throws IllegalArgumentException If the specified target object is not an interface type.
     */
    @SuppressWarnings("unchecked")
    public static <T> T adapt(T target) {
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(target.getClass());
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces.toArray(new Class[] {}),
                new ClientInvocationHandler(target));
        try {
            T client = (T) proxy;
            return client;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Specified target must be an interface type", e);
        }
    }

    public static String inject(String name, String params) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' is required");
        }

        if (params == null) {
            params = "{}";
        }

        Fault fault = container.fault(name, params);

        if (fault == null) {
            throw new IllegalArgumentException("Fault cannot be instantiated with name [" + name + "] and params [" + params + "]");
        }

        if (fault instanceof Validator) {
            Set<Violation> violations = ((Validator) fault).validate();

            if (!violations.isEmpty()) {
                throw new IllegalArgumentException("Parameter 'params' is invalid: " + violations);
            }
        }

        log.info("Injecting fault id [" + fault.id() + "]");
        boolean result = container.scheduler().schedule(fault);

        if (!result) {
            throw new IllegalArgumentException("Fault id [" + fault.id() + "] already exists");
        }

        log.info("Fault id [" + fault.id() + "] injected");
        return fault.id();
    }

    public static void eject(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' is required");
        }

        log.info("Ejecting fault id [" + id + "]");
        boolean result = container.scheduler().unschedule(id);

        if (!result) {
            throw new IllegalArgumentException("Fault id [" + id + "] not found");
        }

        log.info("Fault id [" + id + "] ejected");
    }

    public static Map<String, Class<? extends Fault>> faults() {
        return container.repository().load();
    }

    public static Entry status(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' is required");
        }

        Entry entry = Prodigy.container().scheduler().get(id);

        if (entry == null) {
            throw new IllegalArgumentException("Fault id [" + id + "] not found");
        }

        return entry;
    }

    public static List<Entry> status() {
        return container.scheduler().list();
    }

    public static void push(String name, InputStream jar) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter 'name' is required");
        }

        if (!name.endsWith(".jar")) {
            throw new IllegalArgumentException("Parameter 'name' must be *.jar");
        }

        log.info("Pushing fault jar [" + name + "]");
        Prodigy.container().repository().save(name, jar);
        log.info("Fault jar [" + name + "] pushed");
    }

}
