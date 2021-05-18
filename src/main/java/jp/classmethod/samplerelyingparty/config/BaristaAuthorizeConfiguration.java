package jp.classmethod.samplerelyingparty.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "barista.authorize")
public class BaristaAuthorizeConfiguration {

    public String authorizationEndpoint;

    public String clientId;

    public String clientSecret;

    public String tokenEndpoint;
}
