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

import java.util.List;

import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class HelpCommand implements Command {

    private final Shell shell;

    public HelpCommand(Shell shell) {
        this.shell = shell;
    }
    
    @Override
    public String name() {
        return "help";
    }

    @Override
    public Usage usage() {
        return new Usage("help");
    }

    @Override
    public void run(Line line) throws Exception {
        List<String> args = line.args();

		if (args.isEmpty()) {
            shell.console().println(usage().toString());
            return;
        }
        
		String command = line.args().get(0);
        Command c = shell.commands().get(command);
        
		if (c == null) {
            shell.console().println("no help topic for '" + command + "'");
            return;
        }
        
		shell.console().println(c.usage().toString());
    }

}
