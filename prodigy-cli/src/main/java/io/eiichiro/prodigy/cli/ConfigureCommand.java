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
package io.eiichiro.prodigy.cli;

import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class ConfigureCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private final Path path;

    public ConfigureCommand(Shell shell, Map<String, Object> configuration, Path path) {
        this.shell = shell;
        this.configuration = configuration;
        this.path = path;
    }

    @Override
    public String name() {
        return "configure";
    }

    @Override
    public Usage usage() {
        Usage usage = new Usage("configure [options] <profile> [json-params]");
        usage.option(null, "default", false, "set <profile> as default");
        return usage;
    }

    @Override
    public void run(Line line) throws Exception {
		
	}

}