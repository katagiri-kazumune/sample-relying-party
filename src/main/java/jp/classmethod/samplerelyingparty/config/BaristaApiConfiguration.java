package jp.classmethod.samplerelyingparty.config;

import io.quarkus.arc.config.ConfigProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ConfigProperties(prefix = "barista.api")
public class BaristaApiConfiguration {
    private String getUserEndpoint;
}
