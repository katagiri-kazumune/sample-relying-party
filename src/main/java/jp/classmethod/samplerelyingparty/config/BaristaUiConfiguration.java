package jp.classmethod.samplerelyingparty.config;

import io.quarkus.arc.config.ConfigProperties;

@ConfigProperties(prefix = "barista.ui")
public class BaristaUiConfiguration {
    public String getMfaRegistrationFormEndpoint;
}
