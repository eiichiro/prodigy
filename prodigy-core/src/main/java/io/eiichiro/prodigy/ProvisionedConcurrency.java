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
 * {@code ProvisionedConcurrency} is a helper class to statically initialize 
 * Prodigy core runtime components.
 * If Provisioned Concurrency is enabled on your AWS Lambda functions, you can 
 * easily prewarm core runtime components by invoking {@code #warmup()} method 
 * in the static initialization block to make event processing faster.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public class ProvisionedConcurrency {

    private ProvisionedConcurrency() {}

    /**
     * Statically initializes core runtime components.
     */
    public static void warmup() {
        Prodigy.container();
        Prodigy.configuration();
        // Other class initializations here if needed.
    }

}
