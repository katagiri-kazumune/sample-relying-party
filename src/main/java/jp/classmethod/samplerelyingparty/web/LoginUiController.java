package jp.classmethod.samplerelyingparty.web;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import jp.classmethod.samplerelyingparty.web.oauth.OAuthClient;

/** ログイン UI Controller. */
@Path("/login")
public class LoginUiController {

    private final OAuthClient oAuthClient;

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse httpServletResponse;

    public LoginUiController(OAuthClient oAuthClient) {
        this.oAuthClient = oAuthClient;
    }

    @POST
    public void login() throws IOException {
        var loginUser = LoginUser.fromSession(request);
        if (loginUser.isLoggedIn()) {
            httpServletResponse.sendRedirect("/");
        } else {
            var redirectUri = "http://localhost:8888/oauth/callback";
            httpServletResponse.sendRedirect(
                    oAuthClient.buildAuthorizationRequest(redirectUri, request));
        }
    }
}
