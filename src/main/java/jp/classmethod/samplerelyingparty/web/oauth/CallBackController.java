package jp.classmethod.samplerelyingparty.web.oauth;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;
import jp.classmethod.samplerelyingparty.web.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ログイン UI Controller. */
@Path("/oauth")
public class CallBackController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final OAuthClient oAuthClient;

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse httpServletResponse;

    public CallBackController(OAuthClient oAuthClient) {
        this.oAuthClient = oAuthClient;
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
            var authorizationCodeGrantResponse =
                    oAuthClient.callTokenEndpoint(code, state, request);
            LoginUser.storeSession(authorizationCodeGrantResponse.getUsername(), request);
            httpServletResponse.sendRedirect("/");
        } catch (AuthorizationException e) {
            log.error("error: {}", e.getMessage(), e);
            httpServletResponse.sendError(e.getStatusCode());
        }
    }
}
