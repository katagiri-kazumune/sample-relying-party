package jp.classmethod.samplerelyingparty.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "barista.authorize")
public class ApplicationConfiguration {

    public String authorizationEndpoint;

    public String clientId;

    public String clientSecret;

    public String tokenEndpoint;
}
