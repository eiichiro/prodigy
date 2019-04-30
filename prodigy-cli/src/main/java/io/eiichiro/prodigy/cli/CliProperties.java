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
package io.eiichiro.prodigy.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CliProperties extends Properties {

    private static final long serialVersionUID = 1L;

    private final Log log = LogFactory.getLog(getClass());
    
    public void load() {
        try (InputStream stream = CliProperties.class.getResourceAsStream("/cli.properties")) {
            load(stream);
        } catch (IOException e) {
            log.warn("Unable to load Prodigy CLI properties", e);
        }
    }
    
    public String version(String key) {
        return getProperty("version." + key);
    }
    
    public String dependency(String key) {
        return getProperty("dependency." + key);
    }

}