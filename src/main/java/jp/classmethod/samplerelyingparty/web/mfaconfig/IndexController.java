package jp.classmethod.samplerelyingparty.web.mfaconfig;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import jp.classmethod.samplerelyingparty.config.BaristaAuthorizeConfiguration;
import jp.classmethod.samplerelyingparty.config.BaristaUiConfiguration;

/** MFA 設定画面の Controller. */
@Path("/mfa-config")
public class IndexController {

    private final BaristaUiConfiguration baristaUiConfiguration;

    private final BaristaAuthorizeConfiguration baristaAuthorizeConfiguration;

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse httpServletResponse;

    public IndexController(
            BaristaUiConfiguration baristaUiConfiguration,
            BaristaAuthorizeConfiguration baristaAuthorizeConfiguration) {
        this.baristaUiConfiguration = baristaUiConfiguration;
        this.baristaAuthorizeConfiguration = baristaAuthorizeConfiguration;
    }

    @GET
    public void index() throws IOException {
        httpServletResponse.sendRedirect(buildRedirectUri());
    }

    private String buildRedirectUri() {
        return UriBuilder.fromUri(baristaUiConfiguration.getMfaRegistrationFormEndpoint)
                .queryParam("client_id", baristaAuthorizeConfiguration.clientId)
                .queryParam("redirect_uri", "http://localhost:8888/")
                .toTemplate();
    }
}
