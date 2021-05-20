package jp.classmethod.samplerelyingparty.apiclients;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import jp.classmethod.samplerelyingparty.config.BaristaAuthorizeConfiguration;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;
import jp.classmethod.samplerelyingparty.web.oauth.AuthorizationResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class BaristaClient {

    private static final String STATE_KEY = BaristaClient.class.getName() + "_STATE";

    private static final String REDIRECT_URI_KEY = BaristaClient.class.getName() + "_REDIRECT_URI";

    private static final HttpResponse.BodyHandler<String> BODY_HANDLER =
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

    private final BaristaAuthorizeConfiguration baristaAuthorizeConfiguration;

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

        return UriBuilder.fromUri(baristaAuthorizeConfiguration.getAuthorizationEndpoint())
                .queryParam("state", state)
                .queryParam("client_id", baristaAuthorizeConfiguration.getClientId())
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
    public AuthorizationResult callTokenEndpoint(
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
                    HttpRequest.newBuilder(
                                    URI.create(baristaAuthorizeConfiguration.getTokenEndpoint()))
                            .POST(HttpRequest.BodyPublishers.ofString(bodyString))
                            .header(
                                    "Authorization",
                                    HeaderHelper.buildAuthorizationValue(
                                            baristaAuthorizeConfiguration.getClientId(),
                                            baristaAuthorizeConfiguration.getClientSecret()))
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
            var authorizationResult =
                    new AuthorizationResult(
                            JsonPath.read(responseBody, "$.access_token"),
                            JsonPath.read(responseBody, "$.id_token"),
                            JsonPath.read(responseBody, "$.refresh_token"));
            session.removeAttribute(STATE_KEY);
            session.removeAttribute(REDIRECT_URI_KEY);
            return authorizationResult;
        } catch (AuthorizationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthorizationException(503, e.getMessage(), e);
        }
    }
}
