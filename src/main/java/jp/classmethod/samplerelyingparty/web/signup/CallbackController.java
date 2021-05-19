package jp.classmethod.samplerelyingparty.web.signup;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import jp.classmethod.samplerelyingparty.apiclients.BaristaClient;
import jp.classmethod.samplerelyingparty.web.LoginUser;
import jp.classmethod.samplerelyingparty.web.oauth.AuthorizationResult;

/** ユーザー登録後の Callback. */
@Path("/signup")
public class CallbackController {

    private final BaristaClient baristaClient;

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse httpServletResponse;

    public CallbackController(BaristaClient baristaClient) {
        this.baristaClient = baristaClient;
    }

    /** ユーザー登録後の callback. */
    @GET
    @Path("/callback")
    public void callback() throws IOException {
        // Session 上の認証に関する情報を削除
        LoginUser.removeSession(request);
        AuthorizationResult.removeSession(request);

        // 認可コードフローを回す
        var redirectUri = "http://localhost:8888/oauth/callback";
        httpServletResponse.sendRedirect(
                baristaClient.buildAuthorizationRequest(redirectUri, request));
    }
}
