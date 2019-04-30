package io.eiichiro.prodigy.cli;

import org.eiichiro.reverb.system.Environment;

public class Configuration {

    public static final String PRODIGY_ENDPOINT = "PRODIGY_ENDPOINT";
    
    private String endpoint;

    public void load() {
        endpoint = Environment.getenv(PRODIGY_ENDPOINT);
    }

    /**
     * @return the endpoint
     */
    public String endpoint() {
        return endpoint;
    }

}