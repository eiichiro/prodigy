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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DeleteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetRequest;
import com.amazonaws.services.cloudformation.model.DescribeChangeSetResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.ExecuteChangeSetRequest;
import com.amazonaws.services.cloudformation.model.Output;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.ByteStreams;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class DeployCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private final Path path;

    private AmazonCloudFormation cloudFormation;

    private AmazonS3 s3;

    public DeployCommand(Shell shell, Map<String, Object> configuration, Path path) {
        this.shell = shell;
        this.configuration = configuration;
        this.path = path;
        cloudFormation = AmazonCloudFormationClientBuilder.standard().build();
        s3 = AmazonS3ClientBuilder.standard().build();
    }

    @Override
    public String name() {
        return "deploy";
    }

    @Override
    public Usage usage() {
        return new Usage("deploy <profile> ('deploy core' is not allowed)");
    }

    @Override
    public void run(Line line) throws Exception {
        if (line.args().size() == 1) {
            String profile = line.args().get(0);

            if (profile != null && !profile.isEmpty() && !profile.equals("core")) {
                try {
                    Map<String, String> c = deploy(profile);
                    configuration.put("default", profile);
                    configuration.put(profile, c);

                    if (Files.notExists(path)) {
                        touch();
                    }

                    write();
                    shell.console().prompt("prodigy|" + profile + "> ");
                    log.info("Default profile has been updated with [" + profile + "]");
                    log.debug("Configuration file [" + path + "] updated");
                    read().stream().forEach(log::debug);
                    Command command = new ConfigureCommand(shell, configuration, path);

                    if (!shell.commands().containsKey(command.name())) {
                        shell.register(command);
                        log.debug("Command [" + ConfigureCommand.class.getName() + "] enabled");
                    }

                    command = new InjectCommand(shell, configuration);

                    if (!shell.commands().containsKey(command.name())) {
                        shell.register(command);
                        log.debug("Command [" + InjectCommand.class.getName() + "] enabled");
                    }

                    command = new EjectCommand(shell, configuration);

                    if (!shell.commands().containsKey(command.name())) {
                        shell.register(command);
                        log.debug("Command [" + EjectCommand.class.getName() + "] enabled");
                    }

                    shell.console().println("Prodigy has been successfully deployed for profile [" + profile + "]");
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }

                return;
            }
        }

        shell.console().println("Unsupported usage");
        shell.console().println(usage().toString());
    }

    public void cloudFormation(AmazonCloudFormation cloudFormation) {
        this.cloudFormation = cloudFormation;
    }

    public void s3(AmazonS3 s3) {
        this.s3 = s3;
    }

    private Map<String, String> deploy(String profile) throws Exception {
        List<Output> outputs = deploy("prodigy-core", "/cloudformation/prodigy-core.yml", false, new ArrayList<>());
        CliProperties properties = new CliProperties();
        properties.load();
        String coreJar = properties.dependency("prodigy.core");
        String coreBucket = outputs.get(0).getOutputValue();
        log.info("Uploading core asset [" + coreJar + "] to [" + coreBucket + "]");
        byte[] bytes = ByteStreams.toByteArray(DeployCommand.class.getResourceAsStream("/lib/" + coreJar));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        s3.putObject(coreBucket, coreJar, new ByteArrayInputStream(bytes), metadata);
        log.info("Core asset [" + coreJar + "] uploaded to [" + coreBucket + "]");

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter().withParameterKey("Profile").withParameterValue(profile));
        deploy("prodigy-" + profile + "-validator", "/cloudformation/prodigy-validator.yml", false, parameters,
                Capability.CAPABILITY_NAMED_IAM);
        deploy("prodigy-" + profile + "-controller", "/cloudformation/prodigy-controller.yml", false, parameters,
                Capability.CAPABILITY_NAMED_IAM);

        outputs = deploy("prodigy-" + profile, "/cloudformation/prodigy.yml", true, parameters,
                Capability.CAPABILITY_NAMED_IAM);
        Map<String, String> configuration = new LinkedHashMap<>();
        outputs.forEach(o -> configuration.put(o.getOutputKey().toLowerCase(), o.getOutputValue()));
        String faultsJar = properties.dependency("prodigy.faults");
        String repository = configuration.get("repository");
        log.info("Uploading fault asset [" + faultsJar + "] to [" + repository + "]");
        byte[] bs = ByteStreams.toByteArray(DeployCommand.class.getResourceAsStream("/lib/" + faultsJar));
        ObjectMetadata m = new ObjectMetadata();
        m.setContentLength(bs.length);
        s3.putObject(repository, faultsJar, new ByteArrayInputStream(bs), m);
        log.info("Fault asset [" + faultsJar + "] uploaded to [" + repository + "]");
        return configuration;
    }

    private List<Output> deploy(String stack, String template, boolean update, List<Parameter> parameters,
            Capability... capabilities) throws Exception {
        List<Output> outputs = new ArrayList<>();
        ChangeSetType type;
        boolean exists = false;
        DescribeStacksResult result = cloudFormation.describeStacks();

        for (Stack s : result.getStacks()) {
            if (s.getStackName().equals(stack)) {
                log.info("Stack [" + stack + "] already exists in [" + s.getStackStatus() + "] status");

                if (s.getStackStatus().equals("CREATE_COMPLETE") || s.getStackStatus().equals("UPDATE_COMPLETE")) {
                    exists = true;
                    outputs.addAll(s.getOutputs());
                } else {
                    log.info("Deleting stack [" + stack + "]");
                    DeleteStackRequest request = new DeleteStackRequest().withStackName(stack);
                    cloudFormation.deleteStack(request);
                    wait(() -> {
                        DescribeStacksRequest describeStacksRequest = new DescribeStacksRequest()
                                .withStackName(s.getStackId());
                        String status = cloudFormation.describeStacks(describeStacksRequest).getStacks().get(0)
                                .getStackStatus();

                        if (status.equals("DELETE_COMPLETE")) {
                            return true;
                        } else if (status.equals("DELETE_IN_PROGRESS")) {
                            log.debug("Waiting for deleting stack [" + stack + "]; Status [" + status + "]");
                            return false;
                        } else {
                            throw new IllegalStateException("'DeleteStack' failed in [" + status + "] for a reason of ["
                                    + s.getStackStatusReason() + "]");
                        }
                    });
                    log.info("Stack [" + stack + "] deleted");
                }
            }
        }

        if (!exists) {
            type = ChangeSetType.CREATE;
        } else if (update) {
            type = ChangeSetType.UPDATE;
        } else {
            return outputs;
        }

        log.info("Deploying stack [" + stack + "]");
        String changeSet = "prodigy-" + UUID.randomUUID();
        CreateChangeSetRequest createChangeSetRequest = new CreateChangeSetRequest().withStackName(stack)
                .withChangeSetName(changeSet).withChangeSetType(type).withTemplateBody(read(template));

        if (!parameters.isEmpty()) {
            createChangeSetRequest.withParameters(parameters);
        }

        if (capabilities.length > 0) {
            createChangeSetRequest.withCapabilities(capabilities);
        }

        cloudFormation.createChangeSet(createChangeSetRequest);
        MutableBoolean proceed = new MutableBoolean(true);
        wait(() -> {
            DescribeChangeSetRequest request = new DescribeChangeSetRequest().withStackName(stack)
                    .withChangeSetName(changeSet);
            DescribeChangeSetResult describeChangeSetResult = cloudFormation.describeChangeSet(request);
            String status = describeChangeSetResult.getStatus();

            if (status.equals("CREATE_COMPLETE")) {
                return true;
            } else if (status.equals("CREATE_PENDING") || status.equals("CREATE_IN_PROGRESS")) {
                log.debug("Waiting for creating change set [" + changeSet + "]; Status [" + status + "]");
                return false;
            } else if (status.equals("FAILED") && describeChangeSetResult.getStatusReason()
                    .startsWith("The submitted information didn't contain changes")) {
                log.info("Stack [" + stack + "] does not contain any changes. Deleting change set [" + changeSet + "]");
                DeleteChangeSetRequest deleteChangeSetRequest = new DeleteChangeSetRequest().withStackName(stack)
                        .withChangeSetName(changeSet);
                cloudFormation.deleteChangeSet(deleteChangeSetRequest);
                proceed.setFalse();
                return true;
            } else {
                throw new IllegalStateException("'CreateChangeSet' failed in [" + status + "] for a reason of ["
                        + describeChangeSetResult.getStatusReason() + "]");
            }
        });

        if (proceed.isFalse()) {
            return outputs;
        }

        ExecuteChangeSetRequest executeChangeSetRequest = new ExecuteChangeSetRequest().withStackName(stack)
                .withChangeSetName(changeSet);
        cloudFormation.executeChangeSet(executeChangeSetRequest);
        wait(() -> {
            DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stack);
            Stack s = cloudFormation.describeStacks(request).getStacks().get(0);
            String status = s.getStackStatus();

            if (status.equals("CREATE_COMPLETE") || status.equals("UPDATE_COMPLETE")) {
                outputs.addAll(s.getOutputs());
                return true;
            } else if (status.equals("CREATE_IN_PROGRESS") || status.equals("UPDATE_IN_PROGRESS")
                    || status.equals("REVIEW_IN_PROGRESS") || status.equals("UPDATE_COMPLETE_CLEANUP_IN_PROGRESS")) {
                log.debug("Waiting for executing change set [" + changeSet + "]; Status [" + status + "]");
                return false;
            } else {
                throw new IllegalStateException("'ExecuteChangeSet' failed in [" + status + "] for a reason of ["
                        + s.getStackStatusReason() + "]");
            }
        });
        log.info("Stack [" + stack + "] deployed");
        return outputs;
    }

    private void touch() throws IOException {
        FileUtils.touch(path.toFile());
    }

    private void write() throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.writeValue(path.toFile(), configuration);
    }

    private List<String> read() throws IOException {
        return Files.readAllLines(path);
    }

    private String read(String resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(DeployCommand.class.getResourceAsStream(resource)))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private void wait(Supplier<Boolean> supplier) throws InterruptedException {
        while (!supplier.get()) {
            log.info("Waiting for a response");
            Thread.sleep(5000);
        }
    }

}
