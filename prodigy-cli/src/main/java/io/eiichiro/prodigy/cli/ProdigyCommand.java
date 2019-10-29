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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.eiichiro.reverb.system.Environment;
import org.yaml.snakeyaml.Yaml;

public class ProdigyCommand implements Command {

    private static final String CONFIGURATION = ".prodigy" + File.separator + "config.yml";

    @Override
    public String name() {
        return "prodigy";
    }

    @Override
    public Usage usage() {
        Usage usage = new Usage("prodigy [options] [subcommand] [parameters]");
        usage.option("v", "verbose", false, "turn on debug logging");
        usage.option(null, "config", false, "specify configuration file", "path");
        return usage;
    }

    @Override
    public void run(Line line) throws Exception {
        // Configure Log4j 2 according to the specified verbose option.
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
        Log log = LogFactory.getLog(getClass());

        // Load configuration file.
        Path path = null;

        if (options.containsKey("config")) {
            path = Paths.get(options.get("config"));
        } else {
            path = Paths.get(Environment.getProperty("user.home"), CONFIGURATION);
        }

        Map<String, Object> configuration = new LinkedHashMap<>();
        Shell shell = new Shell();
        shell.register(new DeployCommand(shell, configuration, path));
        log.debug("Command [" + DeployCommand.class.getName() + "] registered");

        if (Files.exists(path)) {
            Yaml yaml = new Yaml();
            configuration = yaml.load(Files.newBufferedReader(path));
            log.info("Configuration file [" + path + "] loaded");
            shell.register(new InjectCommand(shell, configuration));
            shell.register(new EjectCommand(shell, configuration));
            shell.register(new ConfigureCommand(shell, configuration, path));
            shell.register(new DeployCommand(shell, configuration, path));
            shell.register(new StatusCommand(shell, configuration));
            shell.register(new FaultsCommand(shell, configuration));
            log.debug("Command [" + InjectCommand.class.getName() + "] registered");
            log.debug("Command [" + EjectCommand.class.getName() + "] registered");
            log.debug("Command [" + ConfigureCommand.class.getName() + "] registered");
            log.debug("Command [" + DeployCommand.class.getName() + "] registered");
            log.debug("Command [" + StatusCommand.class.getName() + "] registered");
            log.debug("Command [" + FaultsCommand.class.getName() + "] registered");
        } else {
            log.debug("Configuration file [" + path + "] not found");
            log.debug("Command [" + InjectCommand.class.getName() + "] disabled");
            log.debug("Command [" + EjectCommand.class.getName() + "] disabled");
            log.debug("Command [" + ConfigureCommand.class.getName() + "] disabled");
            log.debug("Command [" + DeployCommand.class.getName() + "] disabled");
            log.debug("Command [" + StatusCommand.class.getName() + "] disabled");
            log.debug("Command [" + FaultsCommand.class.getName() + "] disabled");
        }

        List<String> args = line.args();

        if (args.isEmpty()) {
            // Runs into the REPL mode.
            log.debug("Run into the REPL mode");
            shell.register(new HintCommand(shell));
            shell.register(new HelpCommand(shell));
            shell.register(new ExitCommand(shell));
            log.debug("Command [" + HintCommand.class.getName() + "] registered");
            log.debug("Command [" + HelpCommand.class.getName() + "] registered");
            log.debug("Command [" + ExitCommand.class.getName() + "] registered");
            
            Console console = shell.console();
            console.println(Colors.red("__________                   .___.__              "));
            console.println(Colors.red("\\______   \\_______  ____   __| _/|__| ____ ___.__."));
            console.println(Colors.red(" |     ___/\\_  __ \\/  _ \\ / __ | |  |/ ___<   |  |"));
            console.println(Colors.red(" |    |     |  | \\(  <_> ) /_/ | |  / /_/  >___  |"));
            console.println(Colors.red(" |____|     |__|   \\____/\\____ | |__\\___  // ____|"));
            console.println(Colors.red("                              \\/   /_____/ \\/     "));
            console.println("");
            console.println(Colors.red("Chaos Engineering experiment for AWS applications"));
            console.println("");
            console.println(
                    Colors.red("Welcome to Prodigy. Hit the TAB or press 'hint' to display available commands."));
            console.println(Colors.red("Press 'help <command>' to display the detailed information for the command."));
            console.println(Colors.red("Press 'exit' to exit this session."));
            console.println("");
            String profile = "";

            if (configuration.containsKey("default")) {
                profile = (String) configuration.get("default");
                log.debug("Default profile [" + profile + "] configured");
                profile = " - " + profile;
            }
            
            console.prompt("prodigy" + profile + "> ");
            shell.start();
        } else {
            // Runs into the one liner mode.
            log.debug("Run into the one liner mode");
            String l = StringUtils.join(args, " ");
            shell.exec(l);
        }
    }

}
