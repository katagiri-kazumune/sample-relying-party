package jp.classmethod.samplerelyingparty.web.oauth;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import jp.classmethod.samplerelyingparty.apiclients.BaristaClient;
import jp.classmethod.samplerelyingparty.apiclients.GetUserApiClient;
import jp.classmethod.samplerelyingparty.config.BaristaAuthorizeConfiguration;
import jp.classmethod.samplerelyingparty.config.BaristaUiConfiguration;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;
import jp.classmethod.samplerelyingparty.web.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** barista でログイン した後の Callback. */
@Path("/oauth")
public class CallbackController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final BaristaClient baristaClient;

    private final GetUserApiClient getUserApiClient;

    private final BaristaUiConfiguration baristaUiConfiguration;

    private final BaristaAuthorizeConfiguration baristaAuthorizeConfiguration;

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse httpServletResponse;

    public CallbackController(
            BaristaClient baristaClient,
            GetUserApiClient getUserApiClient,
            BaristaAuthorizeConfiguration baristaAuthorizeConfiguration,
            BaristaUiConfiguration baristaUiConfiguration) {
        this.baristaClient = baristaClient;
        this.getUserApiClient = getUserApiClient;
        this.baristaUiConfiguration = baristaUiConfiguration;
        this.baristaAuthorizeConfiguration = baristaAuthorizeConfiguration;
    }

    /**
     * ログイン後の callback.
     *
     * @param code 認可コード
     * @param state state
     */
    @GET
    @Path("/callback")
    public void callback(@QueryParam("code") String code, @QueryParam("state") String state)
            throws IOException {
        try {
            var authorizationResult = baristaClient.callTokenEndpoint(code, state, request);
            AuthorizationResult.storeSession(authorizationResult, request);
            var username = authorizationResult.getUsername();

            if (getUserApiClient.hasMfaSetting(username) == false) {
                // MFA 設定画面にリダイレクト
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
