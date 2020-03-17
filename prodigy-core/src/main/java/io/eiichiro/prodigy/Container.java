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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.eiichiro.prodigy.Scheduler.Entry;

public class Container {

    private static final long PERIOD = 5;

    private Log log = LogFactory.getLog(getClass());

    private final Repository repository;

    private final Scheduler scheduler;

    private ScheduledExecutorService repositorySyncExecutor;

    private ScheduledExecutorService schedulerSyncExecutor;

    private volatile Map<String, Class<? extends Fault>> classes = new HashMap<>();

    private volatile Map<String, Fault> faults = new HashMap<>();

    private volatile Map<String, Interceptor> interceptors = new HashMap<>();

    public Container() {
        this(new Scheduler(), new Repository());
    }

    public Container(Scheduler scheduler, Repository repository) {
        this.scheduler = scheduler;
        this.repository = repository;
        startup();
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public Repository repository() {
        return repository;
    }

    public Fault fault(String name, String params) {
        return fault(name, RandomStringUtils.randomAlphanumeric(8), params);
    }

    public Fault fault(String name, String id, String params) {
        Class<? extends Fault> clazz = classes.get(name);

        if (clazz == null) {
            log.warn("Fault name [" + name + "] not found or not in sync");
            return null;
        }

        try {
            Fault fault = new ObjectMapper().readValue(params, clazz);
            fault.id(id);
            return fault;
        } catch (Exception e) {
            log.warn("Failed to create fault instance", e);
            return null;
        }
    }

    public Map<String, Class<? extends Fault>> classes() {
        return classes;
    }

    public Map<String, Fault> faults() {
        return faults;
    }

    public Map<String, Interceptor> interceptors() {
        return interceptors;
    }

    private void startup() {
        log.info("Starting sync with repository");
        syncRepository();
        ScheduledExecutorService repositorySyncExecutor = Executors.newSingleThreadScheduledExecutor();
        long repositorySyncInterval = interval(PERIOD * 3);
        repositorySyncExecutor.scheduleAtFixedRate(this::syncRepository, repositorySyncInterval, repositorySyncInterval, TimeUnit.SECONDS);
        log.info("Starting sync with scheduler");
        syncScheduler();
        ScheduledExecutorService schedulerSyncExecutor = Executors.newSingleThreadScheduledExecutor();
        long schedulerSyncInterval = interval(PERIOD);
        schedulerSyncExecutor.scheduleAtFixedRate(this::syncScheduler, schedulerSyncInterval, schedulerSyncInterval, TimeUnit.SECONDS);
        this.repositorySyncExecutor = repositorySyncExecutor;
        this.schedulerSyncExecutor = schedulerSyncExecutor;
    }

    private void syncScheduler() {
        try {
            List<Entry> entries = scheduler.list();
            Map<String, Fault> faults = new HashMap<>();
            Map<String, Interceptor> interceptors = new HashMap<>();
            entries.forEach(entry -> {
                Fault fault = fault(entry.getName(), entry.getId(), entry.getParams());

                if (fault == null) {
                    log.warn("Fault cannot be instantiated with name [" + entry.getName() + "] and params [" + entry.getParams() + "]");
                    return;
                }

                faults.put(fault.id(), fault);

                if (entry.getStatus().equals("ACTIVE")) {
                    if (fault instanceof Interceptor) {
                        interceptors.put(fault.id(), (Interceptor) fault);
                    }
                }
            });
            this.faults = faults;
            this.interceptors = interceptors;
            log.debug("'faults' are [" + faults + "]");
            log.debug("'interceptors' are [" + interceptors + "]");
        } catch (Exception e) {
            log.warn("Failed to list fault status", e);
            faults = new HashMap<>();
            interceptors = new HashMap<>();
        }
    }

    private void syncRepository() {
        try {
            classes = repository.load();
            log.debug("'classes' are [" + classes + "]");
        } catch (Exception e) {
            log.warn("Failed to load fault classes", e);
            classes = new HashMap<>();
        }
    }

    private long interval(long period) {
        return RandomUtils.nextLong(period, period * 2);
    }

    public void shutdown() {
        log.info("Stopping sync with scheduler");
        schedulerSyncExecutor.shutdownNow();
        log.info("Stopping sync with repository");
        repositorySyncExecutor.shutdownNow();
    }

}
