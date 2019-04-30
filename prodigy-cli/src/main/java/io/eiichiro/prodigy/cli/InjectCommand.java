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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class InjectCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;
    private final Configuration configuration;

    public InjectCommand(Shell shell, Configuration configuration) {
        this.shell = shell;
        this.configuration = configuration;
    }

    @Override
    public String name() {
        return "inject";
    }

    @Override
    public Usage usage() {
        return new Usage("inject [options] <fault> [json-params]");
    }

    @Override
    public void run(Line line) throws Exception {

    }

}
