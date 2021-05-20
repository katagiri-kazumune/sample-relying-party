package jp.classmethod.samplerelyingparty.config;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ConfigProperties(prefix = "barista.authorize")
public class BaristaAuthorizeConfiguration {

    private String authorizationEndpoint;

    private String clientId;

    private String clientSecret;

    private String tokenEndpoint;
}
