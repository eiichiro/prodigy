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

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.Capability;
import com.amazonaws.services.cloudformation.model.ChangeSetType;
import com.amazonaws.services.cloudformation.model.CreateChangeSetRequest;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;
import org.yaml.snakeyaml.Yaml;

public class DeployCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private final Path path;

    private AmazonCloudFormation cloudFormation = AmazonCloudFormationClientBuilder.defaultClient();

    private AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

    public DeployCommand(Shell shell, Map<String, Object> configuration, Path path) {
        this.shell = shell;
        this.configuration = configuration;
        this.path = path;
    }

    @Override
    public String name() {
        return "deploy";
    }

    @Override
    public Usage usage() {
        return new Usage("deploy <profile>");
    }

    @Override
    public void run(Line line) throws Exception {
        if (line.args().size() == 1) {
            String profile = line.args().get(0);

            if (profile != null && !profile.isEmpty()) {
                try {
                    configuration.put(profile, deploy(profile));
                    configuration.put("default", profile);
                    Yaml yaml = new Yaml();
                    yaml.dump(configuration, Files.newBufferedWriter(path));
                    shell.console().prompt("prodigy - " + profile + "> ");
                    shell.console().println("Default profile has been updated with [" + profile + "]");
                    log.debug("Configuration file [" + path + "] updated");
                    Files.readAllLines(path).stream().forEach(log::debug);
                } catch (Exception e) {
                    shell.console().println(e.getMessage());
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
        String coreJar = properties.dependency("prodigy.core");
        String coreBucket = outputs.get(0).getOutputValue();
        shell.console().println("Uploading core asset [" + coreJar + "] to [" + coreBucket + "]");
        byte[] bytes = IOUtils.toByteArray(DeployCommand.class.getResourceAsStream("/lib/" + coreJar));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        s3.putObject(coreBucket, coreJar, new ByteArrayInputStream(bytes), metadata);
        shell.console().println("Core asset [" + coreJar + "] uploaded to [" + coreBucket + "]");

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter().withParameterKey("Profile").withParameterValue(profile));
        deploy("prodigy-validator-" + profile, "/cloudformation/prodigy-validator.yml", false, parameters,
                Capability.CAPABILITY_NAMED_IAM);
        deploy("prodigy-controller-" + profile, "/cloudformation/prodigy-controller.yml", false, parameters,
                Capability.CAPABILITY_NAMED_IAM);

        parameters.add(new Parameter().withParameterKey("Version")
                .withParameterValue(coreJar.substring("prodigy-core-".length(), coreJar.lastIndexOf(".jar", 0))));
        outputs = deploy("prodigy-" + profile, "/cloudformation/prodigy.yml", true, parameters,
                Capability.CAPABILITY_NAMED_IAM);
        Map<String, String> configuration = new LinkedHashMap<>();
        outputs.forEach(o -> configuration.put(o.getOutputKey().toLowerCase(), o.getOutputValue()));
        String faultsJar = properties.dependency("prodigy.faults");
        String repository = configuration.get("repository");
        shell.console().println("Uploading fault asset [" + faultsJar + "] to [" + repository + "]");
        byte[] bs = IOUtils.toByteArray(DeployCommand.class.getResourceAsStream("/lib/" + faultsJar));
        ObjectMetadata m = new ObjectMetadata();
        m.setContentLength(bs.length);
        s3.putObject(repository, faultsJar, new ByteArrayInputStream(bs), m);
        shell.console().println("Fault asset [" + faultsJar + "] uploaded to [" + repository + "]");
        return configuration;
    }

    private List<Output> deploy(String stack, String template, boolean update, List<Parameter> parameters,
            Capability... capabilities) throws Exception {
        ChangeSetType type;

        if (!exists(stack)) {
            type = ChangeSetType.CREATE;
        } else if (update) {
            type = ChangeSetType.UPDATE;
        } else {
            return new ArrayList<>();
        }

        shell.console().println("Deploying stack [" + stack + "]");
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
        wait(() -> {
            DescribeChangeSetRequest request = new DescribeChangeSetRequest().withStackName(stack)
                    .withChangeSetName(changeSet);
            DescribeChangeSetResult result = cloudFormation.describeChangeSet(request);
            String status = result.getStatus();

            if (status.equals("CREATE_COMPLETE")) {
                return true;
            } else if (status.equals("CREATE_PENDING") || status.equals("CREATE_IN_PROGRESS")) {
                log.debug("Waiting for creating change set [" + changeSet + "]; Status [" + status + "]");
                return false;
            } else {
                throw new IllegalStateException("'CreateChangeSet' failed in [" + status + "] for a reason of ["
                        + result.getStatusReason() + "]");
            }
        });
        ExecuteChangeSetRequest executeChangeSetRequest = new ExecuteChangeSetRequest().withStackName(stack)
                .withChangeSetName(changeSet);
        cloudFormation.executeChangeSet(executeChangeSetRequest);
        List<Output> outputs = new ArrayList<>();
        wait(() -> {
            DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stack);
            Stack s = cloudFormation.describeStacks(request).getStacks().get(0);
            String status = s.getStackStatus();

            if (status.equals("CREATE_COMPLETE") || status.equals("UPDATE_COMPLETE")) {
                outputs.addAll(s.getOutputs());
                return true;
            } else if (status.equals("CREATE_IN_PROGRESS") || status.equals("UPDATE_IN_PROGRESS")
                    || status.equals("REVIEW_IN_PROGRESS")) {
                log.debug("Waiting for executing change set [" + changeSet + "]; Status [" + status + "]");
                return false;
            } else {
                throw new IllegalStateException("'ExecuteChangeSet' failed in [" + status + "] for a reason of ["
                        + s.getStackStatusReason() + "]");
            }
        });
        shell.console().println("Stack [" + stack + "] deployment completed");
        return outputs;
    }

    private boolean exists(String stack) {
        DescribeStacksRequest request = new DescribeStacksRequest().withStackName(stack);
        DescribeStacksResult result = cloudFormation.describeStacks(request);
        return !result.getStacks().isEmpty();
    }

    private String read(String resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(DeployCommand.class.getResourceAsStream(resource)))) {
            StringBuilder builder = new StringBuilder();
            reader.lines().forEach(builder::append);
            return builder.toString();
        }
    }

    private void wait(Supplier<Boolean> supplier) throws InterruptedException {
        while (!supplier.get()) {
            Thread.sleep(50000);
        }
    }

}
