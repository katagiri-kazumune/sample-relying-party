package jp.classmethod.samplerelyingparty.apiclients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import jp.classmethod.samplerelyingparty.config.BaristaApiConfiguration;
import jp.classmethod.samplerelyingparty.exception.ApiException;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class GetUserApiClient {

    private static final HttpResponse.BodyHandler<String> BODY_HANDLER =
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ClientAuthenticationClient clientAuthenticationClient;

    private final BaristaApiConfiguration baristaApiConfiguration;

    /**
     * MFA 設定済みユーザーか？
     *
     * @param username username
     * @return MFA 設定済みの場合、true
     * @throws AuthorizationException 認証でエラーが発生した
     * @throws ApiException API 呼び出しでエラーが発生した
     */
    public boolean hasMfaSetting(String username) {
        var accessToken = clientAuthenticationClient.getClientAccessToken();
        var uri =
                String.format(
                        Locale.ENGLISH, baristaApiConfiguration.getGetUserEndpoint(), username);
        var apiResult = callApi(uri, accessToken);
        return (Boolean) apiResult.get("mfa_email_enabled"); // 必須項目なので手を抜いた
    }

    private HttpRequest buildRequest(String uri, String accessToken) {
        return HttpRequest.newBuilder(URI.create(uri))
                .GET()
                .header("Authorization", String.format(Locale.ENGLISH, "Bearer %s", accessToken))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private Map<String, Object> callApi(String uri, String accessToken) {
        try {
            var request = buildRequest(uri, accessToken);
            var response =
                    HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)
                            .build()
                            .send(request, BODY_HANDLER);
            var statusCode = response.statusCode();
            if (statusCode == 200) {
                return MAPPER.readValue(response.body(), new TypeReference<>() {});
            } else if (statusCode == 401) {
                // もう一度 accessToken を取得してリトライ
                return callApi(uri, clientAuthenticationClient.tokenRefresh());
            } else {
                // 障害
                throw new ApiException(503, "error");
            }
        } catch (AuthorizationException | ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(503, e.getMessage(), e);
        }
    }
}
