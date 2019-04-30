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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class PushCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private CloseableHttpClient httpClient;

    public PushCommand(Shell shell, Map<String, Object> configuration) {
        this.shell = shell;
        this.configuration = configuration;
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("execute-api");
        httpClient = HttpClients.custom().addInterceptorLast(new AWSRequestSigningApacheInterceptor("execute-api",
                signer, DefaultAWSCredentialsProviderChain.getInstance())).build();
    }

    @Override
    public String name() {
        return "push";
    }

    @Override
    public Usage usage() {
        return new Usage("push <fault-jar>");

    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(Line line) throws Exception {
        if (line.args().size() == 1) {
            Path path = Paths.get(line.args().get(0));

            if (Files.notExists(path)) {
                log.warn("Fault jar [" + path + "] does not exist");
                return;
            }

            log.info("Pushing jar [" + path + "]");
            String profile = (String) configuration.get("default");
            HttpPost post = new HttpPost(((Map<String, Object>) configuration.get(profile)).get("endpoint") + "/push");
            post.setEntity(MultipartEntityBuilder.create().addBinaryBody("jar", path.toFile()).build());

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                StatusLine status = response.getStatusLine();
                String content = EntityUtils.toString(response.getEntity(), ContentType.APPLICATION_JSON.getCharset());
                log.debug(content);

                if (status.getStatusCode() == HttpStatus.SC_OK) {
                    shell.console().println("Fault jar has been successfully pushed");
                } else {
                    log.warn("Saving fault jar failed in [" + status + "] for a reason of ["
                    + content + "]");
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
