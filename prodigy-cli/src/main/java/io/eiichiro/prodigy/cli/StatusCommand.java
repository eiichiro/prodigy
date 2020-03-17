package io.eiichiro.prodigy.cli;

import java.util.List;
import java.util.Map;

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

public class StatusCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private CloseableHttpClient httpClient;

    public StatusCommand(Shell shell, Map<String, Object> configuration) {
        this.shell = shell;
        this.configuration = configuration;
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("execute-api");
        httpClient = HttpClients.custom().addInterceptorLast(new AWSRequestSigningApacheInterceptor("execute-api",
                signer, DefaultAWSCredentialsProviderChain.getInstance())).build();
    }

    @Override
    public String name() {
        return "status";
    }

    @Override
    public Usage usage() {
        return new Usage("status [fault-id]");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(Line line) throws Exception {
        String profile = (String) configuration.get("default");
        URIBuilder builder = new URIBuilder(
                ((Map<String, Object>) configuration.get(profile)).get("endpoint") + "/status");

        if (line.args().size() == 1) {
            builder.addParameter("id", line.args().get(0));
        }

        HttpGet get = new HttpGet(builder.build());

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            StatusLine status = response.getStatusLine();
            String content = EntityUtils.toString(response.getEntity(), ContentType.APPLICATION_JSON.getCharset());
            log.debug(content);

            if (status.getStatusCode() == HttpStatus.SC_OK) {
                ObjectMapper mapper = new ObjectMapper();

                if (line.args().size() == 1) {
                    Map<String, Object> output = mapper.readValue(content, new TypeReference<Map<String, Object>>() {
                    });

                    // No need to care about that 'output' is empty.
                    String name = (String) output.get("name");
                    String params = (String) output.get("params");
                    shell.console().println(StringUtils.rightPad("id", 8 + 1) + StringUtils.rightPad("name", name.length() + 1)
                            + StringUtils.rightPad("status", 7 + 1) + "params");
                    shell.console().println(StringUtils.repeat("-", 8) + " " + StringUtils.repeat("-", name.length())
                            + " " + StringUtils.repeat("-", 7) + " " + StringUtils.repeat("-", params.length()));
                    shell.console().println(output.get("id") + " " + name + " "
                            + StringUtils.rightPad((String) output.get("status"), 7) + " " + params);
                } else {
                    List<Map<String, Object>> output = mapper.readValue(content,
                            new TypeReference<List<Map<String, Object>>>() {
                            });

                    if (output.isEmpty()) {
                        shell.console().println("No fault status listed in scheduler");
                        return;
                    }

                    MutableInt length = new MutableInt();

                    if (output.size() == 1) {
                        length.setValue(((String) output.get(0).get("name")).length());
                    } else {
                        output.sort((e1, e2) -> {
                            String n1 = (String) e1.get("name");
                            String n2 = (String) e2.get("name");
                            length.setValue(Math.max(n1.length(), n2.length()));
                            int i = n1.compareTo(n2);
    
                            if (i == 0) {
                                return ((String) e1.get("status")).compareTo((String) e2.get("status"));
                            }
    
                            return i;
                        });
                    }
                    
                    shell.console().println(StringUtils.rightPad("name", length.intValue() + 1)
                            + StringUtils.rightPad("id", 8 + 1) + "status");
                    shell.console().println(StringUtils.repeat("-", length.intValue()) + " "
                            + StringUtils.repeat("-", 8) + " " + StringUtils.repeat("-", 8));
                    output.forEach(e -> shell.console().println(StringUtils.rightPad((String) e.get("name"), length.intValue()) 
                            + " " + e.get("id") + " " + e.get("status")));
                }

            } else {
                log.warn("Listing fault status failed in [" + status + "] for a reason of [" + content + "]");
            }
        }
    }

    public void httpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

}
