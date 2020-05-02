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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@code Invocation} represents Java method invocation on the Prodigy-adapted 
 * client to be intercepted. You can manipulate arguments, invocation result 
 * or exception to be thrown on this object to simulate failures.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public class Invocation {

    private final Object target;

    private final Method method;

    private Object[] args;

    private Object result;

    private Throwable throwable;

    private boolean proceeded;

    /**
     * Creates a new {@code Invocation} instance with the specified target 
     * object and method to invoke.
     * 
     * @param target The target object on which this invocation proceeds.
     * @param method The method this invocation proceeds to invoke.
     */
    public Invocation(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    /**
     * Proceeds actual method invocation and holds the result or thrown 
     * exception. 
     * This method is allowed to invoke only once. If you invoke multiple 
     * times, {@code IllegalStateException} is thrown.
     * 
     * @throws Throwable If this method invocation fails for any reason or you 
     * invoke this method multiple times on the same instance.
     */
    public void proceed() throws Throwable {
        if (proceeded) {
            throw new IllegalStateException("This invocation has already proceeded");
        }

        proceeded = true;

        try {
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            this.throwable = e.getCause();
        }
    }

    /**
     * Returns the target object on which this method invocation is proceeded.
     * 
     * @return The target object on which this method invocation is proceeded.
     */
    public Object target() {
        return target;
    }

    /**
     * Returns the method this invocation proceeds to invoke.
     * 
     * @return The method this invocation proceeds to invoke.
     */
    public Method method() {
        return method;
    }

    /**
     * Returns the invocation arguments.
     * 
     * @return The invocation arguments.
     */
    public Object[] args() {
        return args;
    }

    /**
     * Sets the invocation arguments.
     * 
     * @param args The invocation arguments.
     * @return This instance.
     */
    public Invocation args(Object[] args) {
        this.args = args;
        return this;
    }

    /**
     * Returns the invocation result.
     * 
     * @return The invocation result.
     */
    public Object result() {
        return result;
    }

    /**
     * Sets the invocation result.
     * 
     * @param result The invocation result.
     * @return This instance.
     */
    public Invocation result(Object result) {
        this.result = result;
        return this;
    }

    /**
     * Returns the exception thrown by the method invocation on the target object.
     * 
     * @return The exception thrown by the method invocation on the target object.
     */
    public Throwable throwable() {
        return throwable;
    }

    /**
     * Sets the exception thrown by the method invocation on the target object.
     * 
     * @param throwable The exception thrown by the method invocation on the target object.
     * @return This instance.
     */
    public Invocation throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    /**
     * Returns if method invocation has already proceeded.
     * 
     * @return If method invocation has already proceeded.
     */
    public boolean proceeded() {
        return proceeded;
    }

}
