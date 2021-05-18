package jp.classmethod.samplerelyingparty.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "barista.api")
public class BaristaApiConfiguration {
    public String getUserEndpoint;
}
