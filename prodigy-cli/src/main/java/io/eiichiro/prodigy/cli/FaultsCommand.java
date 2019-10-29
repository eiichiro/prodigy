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

public class FaultsCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final Shell shell;

    private final Map<String, Object> configuration;

    private CloseableHttpClient httpClient;

    public FaultsCommand(Shell shell, Map<String, Object> configuration) {
        this.shell = shell;
        this.configuration = configuration;
        httpClient = HttpClients.custom().addInterceptorLast(new AWSRequestSigningApacheInterceptor("execute-api",
                new AWS4Signer(), DefaultAWSCredentialsProviderChain.getInstance())).build();
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
                List<Map<String, Object>> output = mapper.readValue(content,
                new TypeReference<List<Map<String, Object>>>() {
                });

                if (output.isEmpty()) {
                    shell.console().println("No fault status listed in repository");
                } else {
                    MutableInt name = new MutableInt();
                    MutableInt clazz = new MutableInt();
                    output.sort((e1, e2) -> {
                        String n1 = (String) e1.get("name");
                        String n2 = (String) e2.get("name");
                        String c1 = (String) e1.get("class");
                        String c2 = (String) e2.get("class");
                        name.setValue(Math.max(n1.length(), n2.length()));
                        clazz.setValue(Math.max(c1.length(), c2.length()));
                        int i = n1.compareTo(n2);

                        if (i == 0) {
                            return c1.compareTo(c2);
                        }

                        return i;
                    });
                    shell.console().println(StringUtils.rightPad("name", name.intValue() + 1) + "class");
                    shell.console().println(StringUtils.repeat("-", name.intValue()) + " " + StringUtils.repeat("-", clazz.intValue()));
                    output.forEach(e -> shell.console().println(StringUtils.rightPad((String) e.get("name"), name.intValue()) + e.get("class")));
                }
                
            } else {
                shell.console().println("Listing fault classes failed in [" + status + "] for a reason of [" + content + "]");
            }
        }
    }

    public void httpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

}
