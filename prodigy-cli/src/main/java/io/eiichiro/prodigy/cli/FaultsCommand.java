package io.eiichiro.prodigy.cli;

import java.util.Map;
import java.util.TreeMap;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eiichiro.ash.Command;
import org.eiichiro.ash.Line;
import org.eiichiro.ash.Shell;
import org.eiichiro.ash.Usage;

public class FaultsCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private CloseableHttpClient httpClient;

    public FaultsCommand(Shell shell, Map<String, Object> configuration) {
        this.shell = shell;
        this.configuration = configuration;
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("execute-api");
        httpClient = HttpClients.custom().addInterceptorLast(new AWSRequestSigningApacheInterceptor("execute-api",
                signer, DefaultAWSCredentialsProviderChain.getInstance())).build();
    }

    @Override
    public String name() {
        return "faults";
    }

    @Override
    public Usage usage() {
        return new Usage("faults");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(Line line) throws Exception {
        String profile = (String) configuration.get("default");
        URIBuilder builder = new URIBuilder(
                ((Map<String, Object>) configuration.get(profile)).get("endpoint") + "/faults");
        HttpGet get = new HttpGet(builder.build());

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            StatusLine status = response.getStatusLine();
            String content = EntityUtils.toString(response.getEntity(), ContentType.APPLICATION_JSON.getCharset());
            log.debug(content);

            if (status.getStatusCode() == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> output = mapper.readValue(content,
                new TypeReference<Map<String, String>>() {
                });

                if (output.isEmpty()) {
                    shell.console().println("No fault classes listed in repository");
                } else {
                    MutableInt name = new MutableInt();
                    MutableInt clazz = new MutableInt();
                    Map<String, String> o = new TreeMap<>((e1, e2) -> {
                        name.setValue(Math.max(e1.length(), e2.length()));
                        clazz.setValue(Math.max(output.get(e1).length(), output.get(e2).length()));
                        return e1.compareTo(e2);
                    });
                    o.putAll(output);
                    shell.console().println(StringUtils.rightPad("name", name.intValue() + 1) + "class");
                    shell.console().println(StringUtils.repeat("-", name.intValue()) + " " + StringUtils.repeat("-", clazz.intValue()));
                    o.forEach((k, v) -> shell.console().println(StringUtils.rightPad(k, name.intValue() + 1) + v));
                }
                
            } else {
                log.warn("Listing fault classes failed in [" + status + "] for a reason of [" + content + "]");
            }
        }
    }

    public void httpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

}
