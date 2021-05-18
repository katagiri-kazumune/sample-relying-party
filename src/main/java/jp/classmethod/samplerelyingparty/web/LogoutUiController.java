package jp.classmethod.samplerelyingparty.web;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/** ログアウト UI Controller. */
@Path("/logout")
public class LogoutUiController {

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse httpServletResponse;

    @POST
    public void logout() throws IOException {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        httpServletResponse.sendRedirect("/");
    }
}
