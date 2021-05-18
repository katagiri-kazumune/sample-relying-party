package jp.classmethod.samplerelyingparty.apiclients;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.enterprise.context.ApplicationScoped;
import jp.classmethod.samplerelyingparty.config.BaristaAuthorizeConfiguration;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;

@ApplicationScoped
public class ClientAuthenticationClient {

    private static final HttpResponse.BodyHandler<String> BODY_HANDLER =
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

    private final BaristaAuthorizeConfiguration baristaAuthorizeConfiguration;

    public ClientAuthenticationClient(BaristaAuthorizeConfiguration baristaAuthorizeConfiguration) {
        this.baristaAuthorizeConfiguration = baristaAuthorizeConfiguration;
    }

    private String clientAccessToken;

    public String getClientAccessToken() {
        if (clientAccessToken != null) {
            return clientAccessToken;
        }
        return tokenRefresh();
    }

    public String tokenRefresh() {
        clientAccessToken = getAccessTokenForClientAuthentication();
        return clientAccessToken;
    }

    private String getAccessTokenForClientAuthentication() {
        var bodyString = "grant_type=client_credentials";
        try {
            var tokenEndpointRequest =
                    HttpRequest.newBuilder(new URI(baristaAuthorizeConfiguration.tokenEndpoint))
                            .POST(HttpRequest.BodyPublishers.ofString(bodyString))
                            .header(
                                    "Authorization",
                                    HeaderHelper.buildAuthorizationValue(
                                            baristaAuthorizeConfiguration.clientId,
                                            baristaAuthorizeConfiguration.clientSecret))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .timeout(Duration.ofSeconds(10))
                            .build();
            var response =
                    HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .build()
                            .send(tokenEndpointRequest, BODY_HANDLER);
            if (response.statusCode() != 200) {
                throw new AuthorizationException(400, "invalid request");
            }
            var responseBody = response.body();
            return JsonPath.read(responseBody, "$.access_token");
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthorizationException(503, e.getMessage(), e);
        }
    }
}
