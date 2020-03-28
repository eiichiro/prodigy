/*
 * Copyright (C) 2019-2020 Eiichiro Uchiumi and The Prodigy Authors. All 
 * Rights Reserved.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Invocation {

    private Object target;

    private Method method;

    private Object[] args;

    private Object result;

    private Throwable throwable;

    private boolean proceeded;

    public void proceed() throws Throwable {
        if (proceeded) {
            throw new IllegalStateException("");
        }

        proceeded = true;

        try {
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            this.throwable = e.getCause();
        }
    }

    public Object target() {
        return target;
    }

    public Invocation target(Object target) {
        this.target = target;
        return this;
    }

    public Method method() {
        return method;
    }

    public Invocation method(Method method) {
        this.method = method;
        return this;
    }

    public Object[] args() {
        return args;
    }

    public Invocation args(Object[] args) {
        this.args = args;
        return this;
    }

    public Object result() {
        return result;
    }

    public Invocation result(Object result) {
        this.result = result;
        return this;
    }

    public Throwable throwable() {
        return throwable;
    }

    public Invocation throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public boolean proceeded() {
        return proceeded;
    }

}
