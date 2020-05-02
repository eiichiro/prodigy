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

import org.eiichiro.reverb.system.Environment;

public class Configuration {

    public static final String PRODIGY_FAULT_SCHEDULER = "PRODIGY_FAULT_SCHEDULER";
    
    public static final String PRODIGY_FAULT_REPOSITORY = "PRODIGY_FAULT_REPOSITORY";
    
    private final String scheduler;

    private final String repository;

    public Configuration() {
        this(Environment.getenv(PRODIGY_FAULT_SCHEDULER), Environment.getenv(PRODIGY_FAULT_REPOSITORY));
    }

    public Configuration(String scheduler, String repository) {
        this.scheduler = scheduler;
        this.repository = repository;
    }

    /**
     * @return the scheduler
     */
    public String scheduler() {
        return scheduler;
    }

    /**
     * @return the repository
     */
    public String repository() {
        return repository;
    }

}