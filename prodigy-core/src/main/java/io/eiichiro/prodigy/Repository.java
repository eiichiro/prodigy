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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eiichiro.reverb.lang.UncheckedException;
import org.eiichiro.reverb.system.Environment;

public class Repository {

    private static final String CACHE = "prodigy" + File.separator + "faults";

    private final Log log = LogFactory.getLog(getClass());

    private final AmazonS3 s3;

    private Map<String, Class<? extends Fault>> faults = new HashMap<>();

    public Repository() {
        this(AmazonS3ClientBuilder.defaultClient());
    }

    public Repository(AmazonS3 s3) {
        this.s3 = s3;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Class<? extends Fault>> load() {
        String repository = Prodigy.configuration().repository();
        ListObjectsV2Result result = s3.listObjectsV2(repository);
        List<String> objects = new ArrayList<>();
        result.getObjectSummaries().forEach(s -> objects.add(s.getKey()));

        while (result.isTruncated()) {
            ListObjectsV2Request request = new ListObjectsV2Request();
            result = s3.listObjectsV2(
                    request.withBucketName(repository).withContinuationToken(result.getContinuationToken()));
            result.getObjectSummaries().forEach(s -> objects.add(s.getKey()));
        }

        try {
            String tmpdir = Environment.getProperty("java.io.tmpdir");
            List<String> files = new ArrayList<>();

            if (Files.notExists(Paths.get(tmpdir, CACHE))) {
                Files.createDirectories(Paths.get(tmpdir, CACHE));
            }

            Files.list(Paths.get(tmpdir, CACHE)).forEach(p -> files.add(p.getFileName().toString()));
            log.debug("Saved fault jars are [" + objects + "]");
            log.debug("Cached fault jars are [" + files + "]");
            Collection<String> add = CollectionUtils.subtract(objects, files);
            Collection<String> remove = CollectionUtils.subtract(files, objects);
            add.stream().forEach(s -> {
                File file = Paths.get(tmpdir, CACHE, s).toFile();
                s3.getObject(new GetObjectRequest(repository, s), file);
            });
    
            for (String s : remove) {
                Files.deleteIfExists(Paths.get(tmpdir, CACHE, s));
            }
    
            if (!add.isEmpty() || !remove.isEmpty()) {
                List<URL> urls = new ArrayList<>();
    
                for (Object path : Files.list(Paths.get(tmpdir, CACHE)).toArray()) {
                    urls.add(((Path) path).toUri().toURL());
                }
    
                log.debug("Fault jars to be loaded are [" + urls + "]");
                ClassLoader loader = URLClassLoader.newInstance(urls.toArray(new URL[0]),
                        Thread.currentThread().getContextClassLoader());
                Enumeration<URL> enumeration = loader.getResources("prodigy.faults");
                Map<String, Class<? extends Fault>> faults = new HashMap<>();
    
                while (enumeration.hasMoreElements()) {
                    URL url = enumeration.nextElement();
                    log.debug("'prodigy.faults' file to be loaded is [" + url + "]");

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        reader.lines().forEach(l -> {
                            try {
                                log.debug("Fault class to be loaded is [" + l + "]");
                                Class<?> clazz = Class.forName(l, true, loader);
        
                                if (!Fault.class.isAssignableFrom(clazz)) {
                                    log.warn("Fault class [" + clazz + "] must inherit " + Fault.class);
                                    return;
                                }
        
                                Named named = clazz.getAnnotation(Named.class);
                                
                                if (named != null) {
                                    String name = named.value();
                                    
                                    if (name == null || name.isEmpty()) {
                                        log.warn("Value of @Named annotation on [" + clazz + "] must not be [" + name + "]");
                                        return;
                                    }
        
                                    faults.put(name, (Class<? extends Fault>) clazz);
                                } else {
                                    faults.put(clazz.getSimpleName(), (Class<? extends Fault>) clazz);
                                }
        
                            } catch (ClassNotFoundException e) {
                                log.warn("Fault class [" + l + "] not found", e);
                            }
                        });
                    }
                }
    
                this.faults = faults;
            }
    
            return faults;
        } catch (IOException e) {
            throw new UncheckedException(e);
        }
    }

    public void save(String name, InputStream jar) {
        s3.putObject(Prodigy.configuration().repository(), name, jar, new ObjectMetadata());
    }
    
}
