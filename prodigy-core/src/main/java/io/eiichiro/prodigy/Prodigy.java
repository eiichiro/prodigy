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

import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

/**
 * {@code Prodigy} is the helper class to adapt application objects to Prodigy 
 * and give an access to core runtime components internally.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public final class Prodigy {

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

}
