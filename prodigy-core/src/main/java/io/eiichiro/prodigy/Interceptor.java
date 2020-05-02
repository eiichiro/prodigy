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

/**
 * {@code Interceptor} is the interface that fault classes need to implement 
 * if they intend to apply themselves by intercepting resource invocations 
 * like downstream API call.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public interface Interceptor {

    /**
     * Applies this fault to target Java method invocation. 
     * You can manipulate arguments, invocation result or exception to be 
     * thrown on the specified {@code Invocation} object to simulate failures, 
     * and also you can proceed actual invocation with {@code Invocation#proceed()} 
     * method. This method is called back on all Prodigy-adapted method 
     * invocations while this fault is active, so you need to notify whether 
     * this fault has been applied to the specified invocation or not as the 
     * return value. If you return {@code true}, the (manipulated) invocation 
     * result or exception is got back to the client immediately. If you 
     * return {@code false}, other interceptor is continued to call back.
     * 
     * @param invocation Captured Java method invocation. You can manipulate 
     * arguments, invocation result or exception to be thrown on this object 
     * to simulate failures. If both invocation result and exception are set 
     * on this object, the exception is preferentially thrown to the client.
     * @return {@code true} if this fault has been applied to the specified 
     * invocation and the (manipulated) invocation result or exception needs 
     * to be got back to the client immediately. {@code false} if this fault 
     * has not been applied to the specified invocation or other interceptor 
     * is explicitly continued to call back (be careful of side effect caused 
     * by applying multiple faults to single invocation).
     * @throws Throwable If fails to apply this fault to the specified 
     * invocation for any reason. Not manipulated exception which needs to be 
     * got back to the client.
     * @see Prodigy#adapt(Object)
     * @see Invocation
     */
    public boolean apply(Invocation invocation) throws Throwable;

}
