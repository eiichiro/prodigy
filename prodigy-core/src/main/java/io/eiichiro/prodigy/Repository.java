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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
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

    private static final String CACHE = Environment.getProperty("java.io.tmpdir") + "prodigy" + File.separator + "faults";

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
        List<String> objects = new ArrayList<>();   // Saved fault jars in S3
        String repository = Prodigy.configuration().repository();
        ListObjectsV2Result result = s3.listObjectsV2(repository);
        result.getObjectSummaries().forEach(s -> objects.add(s.getKey()));

        while (result.isTruncated()) {
            ListObjectsV2Request request = new ListObjectsV2Request();
            result = s3.listObjectsV2(request.withBucketName(repository).withContinuationToken(result.getContinuationToken()));
            result.getObjectSummaries().forEach(s -> objects.add(s.getKey()));
        }

        List<String> files = new ArrayList<>();     // Cached fault jars in local

        try {
            if (!exists(CACHE)) {
                create(CACHE);
            }

            list(CACHE).forEach(f -> {
                files.add(Paths.get(f).getFileName().toString());
            });
            log.debug("Saved fault jars are [" + objects + "]");
            log.debug("Cached fault jars are [" + files + "]");

            Collection<String> add = CollectionUtils.subtract(objects, files);
            Collection<String> remove = CollectionUtils.subtract(files, objects);
            create(CACHE, add);
            delete(CACHE, remove);
    
            if (!add.isEmpty() || !remove.isEmpty()) {
                List<URL> urls = new ArrayList<>();

                for (String file : list(CACHE)) {
                    urls.add(Paths.get(file).toUri().toURL());
                }
    
                log.debug("Fault jars to be loaded are [" + urls + "]");

                Map<String, Class<? extends Fault>> faults = new HashMap<>();
                ClassLoader loader = newLoader(urls);
                readFaults(loader).forEach(l -> {
                    if (l.startsWith("#")) {
                        return;
                    }

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

    private boolean exists(String dir) {
        return Files.exists(Paths.get(dir));
    }

    private void create(String dir) throws IOException {
        Files.createDirectories(Paths.get(dir));
    }

    private List<String> list(String dir) throws IOException {
        List<String> files = new ArrayList<>();
        Files.list(Paths.get(dir)).forEach(p -> {
            files.add(p.toString());
        });
        return files;
    }

    private void create(String dir, Collection<String> files) {
        files.stream().forEach(s -> {
            File file = Paths.get(dir, s).toFile();
            s3.getObject(new GetObjectRequest(Prodigy.configuration().repository(), s), file);
        });
    }

    private void delete(String dir, Collection<String> files) throws IOException {
        for (String s : files) {
            Files.deleteIfExists(Paths.get(dir, s));
        }
    }

    private ClassLoader newLoader(List<URL> urls) {
        return URLClassLoader.newInstance(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
    }

    private List<String> readFaults(ClassLoader loader) throws IOException {
        List<String> faults = new ArrayList<>();
        Enumeration<URL> enumeration = loader.getResources("prodigy.faults");

        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            log.debug("'prodigy.faults' file to be read is [" + url + "]");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                reader.lines().forEach(faults::add);
            }
        }

        return faults;
    }
    
}
