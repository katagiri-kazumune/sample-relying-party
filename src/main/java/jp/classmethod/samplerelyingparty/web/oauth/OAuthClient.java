package jp.classmethod.samplerelyingparty.web.oauth;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import jp.classmethod.samplerelyingparty.config.ApplicationConfiguration;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OAuthClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String STATE_KEY = OAuthClient.class.getName() + "_STATE";

    private static final String REDIRECT_URI_KEY = OAuthClient.class.getName() + "REDIRECT_URI";

    private final ApplicationConfiguration applicationConfiguration;

    public OAuthClient(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    /**
     * 認可リクエスト作成.
     *
     * @param redirectUri 認可コードの送信先リダイレクト URI
     * @param request request
     * @return Authorization Endpoint の URI
     */
    public String buildAuthorizationRequest(String redirectUri, HttpServletRequest request) {
        var state = UUID.randomUUID().toString();
        var session = request.getSession(true);
        session.setAttribute(STATE_KEY, state);
        session.setAttribute(REDIRECT_URI_KEY, redirectUri);

        return UriBuilder.fromUri(applicationConfiguration.authorizationEndpoint)
                .queryParam("state", state)
                .queryParam("client_id", applicationConfiguration.clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .toTemplate();
    }

    /**
     * Token Endpoint 呼び出し.
     *
     * @param code リダイレクト URI で受け取った認可コード
     * @param state リダイレクト URI で受け取った state
     * @param request request
     * @return レスポンス
     * @throws AuthorizationException 呼び出しに失敗した
     */
    public AuthorizationCodeGrantResponse callTokenEndpoint(
            String code, String state, HttpServletRequest request) {
        var session = request.getSession(true);
        if (Objects.equals(session.getAttribute(STATE_KEY), state) == false) {
            // state が不正
            throw new AuthorizationException(400, "invalid state");
        }

        // Token Endpoint を呼び出し、code とアクセストークンを引き換える
        try {
            var bodyString =
                    String.format(
                            Locale.ENGLISH,
                            "grant_type=authorization_code&code=%s&redirect_uri=%s",
                            code,
                            session.getAttribute(REDIRECT_URI_KEY));
            var tokenEndpointRequest =
                    HttpRequest.newBuilder(new URI(applicationConfiguration.tokenEndpoint))
                            .POST(HttpRequest.BodyPublishers.ofString(bodyString))
                            .header(
                                    "Authorization",
                                    buildAuthorizationValue(
                                            applicationConfiguration.clientId,
                                            applicationConfiguration.clientSecret))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .timeout(Duration.ofSeconds(10))
                            .build();
            var bodyHandler = HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
            var response =
                    HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .build()
                            .send(tokenEndpointRequest, bodyHandler);
            if (response.statusCode() != 200) {
                throw new AuthorizationException(400, "invalid request");
            }
            var responseBody = response.body();
            var authorizationCodeGrantResponse =
                    new AuthorizationCodeGrantResponse(
                            JsonPath.read(responseBody, "$.access_token"),
                            JsonPath.read(responseBody, "$.id_token"),
                            JsonPath.read(responseBody, "$.refresh_token"));
            session.removeAttribute(STATE_KEY);
            session.removeAttribute(REDIRECT_URI_KEY);
            return authorizationCodeGrantResponse;
        } catch (Exception e) {
            throw new AuthorizationException(503, e.getMessage(), e);
        }
    }

    private static String buildAuthorizationValue(String username, String password) {
        return "Basic "
                + Base64.getEncoder()
                        .encodeToString(
                                (username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
