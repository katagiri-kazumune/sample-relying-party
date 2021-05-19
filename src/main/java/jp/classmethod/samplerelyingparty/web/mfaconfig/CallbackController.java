package jp.classmethod.samplerelyingparty.web.mfaconfig;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import jp.classmethod.samplerelyingparty.apiclients.GetUserApiClient;
import jp.classmethod.samplerelyingparty.config.BaristaAuthorizeConfiguration;
import jp.classmethod.samplerelyingparty.config.BaristaUiConfiguration;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;
import jp.classmethod.samplerelyingparty.web.LoginUser;
import jp.classmethod.samplerelyingparty.web.oauth.AuthorizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** MFA 設定完了した後の Callback. */
@Path("/mfa-config")
public class CallbackController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final GetUserApiClient getUserApiClient;

    private final BaristaUiConfiguration baristaUiConfiguration;

    private final BaristaAuthorizeConfiguration baristaAuthorizeConfiguration;

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse httpServletResponse;

    public CallbackController(
            GetUserApiClient getUserApiClient,
            BaristaAuthorizeConfiguration baristaAuthorizeConfiguration,
            BaristaUiConfiguration baristaUiConfiguration) {
        this.getUserApiClient = getUserApiClient;
        this.baristaUiConfiguration = baristaUiConfiguration;
        this.baristaAuthorizeConfiguration = baristaAuthorizeConfiguration;
    }

    /** MFA 設定後の callback. */
    @GET
    @Path("/callback")
    public void callback() throws IOException {
        try {
            var authorizationResult = AuthorizationResult.fromSession(request);
            if (authorizationResult == null) {
                // Session 上に認証情報が存在しない場合、TOP 画面へ
                httpServletResponse.sendRedirect("/");
                return;
            }

            var username = authorizationResult.getUsername();
            if (getUserApiClient.hasMfaSetting(username) == false) {
                // まだ有効でない場合、MFA 設定画面にリダイレクト
                httpServletResponse.sendRedirect(buildRedirectUri());
                return;
            }

            LoginUser.storeSession(username, request);
            httpServletResponse.sendRedirect("/");
        } catch (AuthorizationException e) {
            log.error("error: {}", e.getMessage(), e);
            httpServletResponse.sendError(e.getStatusCode());
        }
    }

    private String buildRedirectUri() {
        return UriBuilder.fromUri(baristaUiConfiguration.getMfaRegistrationFormEndpoint)
                .queryParam("client_id", baristaAuthorizeConfiguration.clientId)
                .queryParam("redirect_uri", "http://localhost:8888/mfa-config/callback")
                .toTemplate();
    }
}
