package io.eiichiro.prodigy.cli;

import java.util.LinkedHashMap;
import java.util.Map;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class EjectCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private HttpClientBuilder httpClientBuilder;

    public EjectCommand(Shell shell, Map<String, Object> configuration) {
        this.shell = shell;
        this.configuration = configuration;
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("execute-api");
        httpClientBuilder = HttpClients.custom().addInterceptorLast(new AWSRequestSigningApacheInterceptor("execute-api",
                signer, DefaultAWSCredentialsProviderChain.getInstance()));
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

            try (CloseableHttpResponse response = httpClientBuilder.build().execute(post)) {
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

    public void httpClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
    }

}
