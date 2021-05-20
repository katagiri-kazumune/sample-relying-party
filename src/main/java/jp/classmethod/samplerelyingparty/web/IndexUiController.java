package jp.classmethod.samplerelyingparty.web;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

/** Index UI Controller. */
@Slf4j
@Path("")
public class IndexUiController {

    @Context private HttpServletRequest request;

    @CheckedTemplate
    public static class Templates {

        public static native TemplateInstance index(LoginUser loginUser);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        log.info("call index.");
        return Templates.index(LoginUser.fromSession(request));
    }
}
