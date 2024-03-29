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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

public class EjectCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private CloseableHttpClient httpClient;

    public EjectCommand(Shell shell, Map<String, Object> configuration) {
        this.shell = shell;
        this.configuration = configuration;
        httpClient = HttpClients.custom().addInterceptorLast(new AwsRequestSigningApacheInterceptor("execute-api", 
                Aws4Signer.create(), DefaultCredentialsProvider.create(), DefaultAwsRegionProviderChain.builder().build().getRegion())).build();
    }

    @Override
    public String name() {
        return "eject";
    }

    @Override
    public Usage usage() {
        return new Usage("eject <fault-id>");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(Line line) throws Exception {
        if (line.args().size() == 1) {
            Map<String, String> input = new LinkedHashMap<>();
            String id = line.args().get(0);
            input.put("id", id);
            String json = new ObjectMapper().writeValueAsString(input);
            log.info("Ejecting fault id [" + id + "]");
            log.debug(json);
            String profile = (String) configuration.get("default");
            HttpPost post = new HttpPost(((Map<String, Object>) configuration.get(profile)).get("endpoint") + "/eject");
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                StatusLine status = response.getStatusLine();
                String content = EntityUtils.toString(response.getEntity(), ContentType.APPLICATION_JSON.getCharset());
                log.debug(content);

                if (status.getStatusCode() == HttpStatus.SC_OK) {
                    shell.console().println("Fault has been successfully ejected");
                } else {
                    log.warn("Ejecting fault failed in [" + status + "] for a reason of [" + content + "]");
                }
            }

            return;
        }

        shell.console().println("Unsupported usage");
        shell.console().println(usage().toString());
    }

    public void httpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

}
