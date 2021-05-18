package jp.classmethod.samplerelyingparty.web;

import java.util.Collections;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

public class SessionRegenerator {

    /**
     * Session 付け替え.
     *
     * <p>新しい Session を作成し、既存の Session 内の情報を設定します。 既存の Session は invalidate します。
     *
     * @param request request
     */
    public static void regenerateSession(HttpServletRequest request) {
        var session = request.getSession(true);
        var storedValues =
                Collections.list(session.getAttributeNames()).stream()
                        .collect(Collectors.toMap(attribute -> attribute, session::getAttribute));
        session.invalidate();

        var newSession = request.getSession(true);
        storedValues.forEach(newSession::setAttribute);
    }
}
