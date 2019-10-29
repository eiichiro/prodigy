/*
 * Copyright (C) 2019 Eiichiro Uchiumi and The Prodigy Authors. All Rights Reserved.
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

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Container {

    private static Log log = LogFactory.getLog(Container.class);

    private final Scheduler scheduler;

    private final Repository repository;

    public Container() {
        this(new Scheduler(), new Repository());
    }

    public Container(Scheduler scheduler, Repository repository) {
        this.scheduler = scheduler;
        this.repository = repository;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public Repository repository() {
        return repository;
    }

    public Fault fault(String id) {
        return null;
    }

    public Fault fault(String name, Map<String, Object> params) {
        return null;
    }

    public Set<Fault> faults() {
        return null;
    }

}
