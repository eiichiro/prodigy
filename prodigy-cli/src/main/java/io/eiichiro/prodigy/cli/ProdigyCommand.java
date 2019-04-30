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
import java.util.Map;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.eiichiro.ash.Colors;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Console;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class ProdigyCommand implements Command {

    @Override
    public String name() {
        return "prodigy";
    }

    @Override
    public Usage usage() {
        Usage usage = new Usage("prodigy [options] [subcommand] [parameters]");
        usage.option("v", "verbose", false, "turn on debug logging");
        return usage;
    }

    @Override
    public void run(Line line) throws Exception {
        Map<String, String> options = line.options();
        boolean verbose = options.containsKey("v") || options.containsKey("verbose");
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR);
        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
                ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern",
                "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n%throwable"));
        builder.add(appenderBuilder);
        builder.add(builder.newRootLogger((verbose) ? Level.DEBUG : Level.INFO).add(builder.newAppenderRef("Stdout")));
        Configurator.initialize(builder.build());

        Configuration configuration = new Configuration();
        configuration.load();
        String endpoint = configuration.endpoint();
        Preconditions.checkNotNull(endpoint,
                "Environment variable '" + Configuration.PRODIGY_ENDPOINT + "' must not be [" + endpoint + "]");

        Shell shell = new Shell();
        shell.register(new InjectCommand(shell, configuration));
        List<String> args = line.args();

        if (args.isEmpty()) {
            // Runs into the REPL mode.
            shell.register(new HintCommand(shell));
            shell.register(new HelpCommand(shell));
            shell.register(new ExitCommand(shell));
            Console console = shell.console();
            console.println(Colors.red("__________                   .___.__              "));
            console.println(Colors.red("\\______   \\_______  ____   __| _/|__| ____ ___.__."));
            console.println(Colors.red(" |     ___/\\_  __ \\/  _ \\ / __ | |  |/ ___<   |  |"));
            console.println(Colors.red(" |    |     |  | \\(  <_> ) /_/ | |  / /_/  >___  |"));
            console.println(Colors.red(" |____|     |__|   \\____/\\____ | |__\\___  // ____|"));
            console.println(Colors.red("                              \\/   /_____/ \\/     "));
            console.println("");
            console.println(Colors.red("Chaos Engineering experiments for AWS applications"));
            console.println("");
            console.println(
                    Colors.red("Welcome to Prodigy. Hit the TAB or press 'hint' to display available commands."));
            console.println(Colors.red("Press 'help command' to display the detailed information for the command."));
            console.println(Colors.red("Press 'exit' to exit this session."));
            console.println("");
            console.prompt("prodigy" + "> ");
            shell.start();
        } else {
            String l = StringUtils.join(args, " ");
            shell.exec(l);
        }
    }

}
