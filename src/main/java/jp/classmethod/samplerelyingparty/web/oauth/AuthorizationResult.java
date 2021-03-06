package jp.classmethod.samplerelyingparty.web.oauth;

import com.nimbusds.jwt.SignedJWT;
import java.io.Serializable;
import java.text.ParseException;
import javax.servlet.http.HttpServletRequest;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;
import lombok.Value;

/** 認証結果. */
@Value
public class AuthorizationResult implements Serializable {

    private static final String SESSION_KEY = AuthorizationResult.class.getName();

    String accessToken;

    String idToken;

    String refreshToken;

    /**
     * Session 上のインスタンス取得.
     *
     * @param request request
     * @return 認証結果(Session に存在しない場合、null)
     */
    public static AuthorizationResult fromSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (AuthorizationResult) session.getAttribute(SESSION_KEY);
    }

    /**
     * Session へ設定.
     *
     * @param authorizationResult 認証結果
     * @param request request
     */
    public static void storeSession(
            AuthorizationResult authorizationResult, HttpServletRequest request) {
        var session = request.getSession(true);
        session.setAttribute(SESSION_KEY, authorizationResult);
    }

    /**
     * Session から削除.
     *
     * @param request request
     */
    public static void removeSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(SESSION_KEY);
    }

    public String getUsername() {
        if (idToken != null) {
            return getUsernameForIdToken();
        }

        // TODO access_token が存在する場合、Token Introspection Endpoint を呼び出し、username を取得
        return "dummy_user_1234";
    }

    private String getUsernameForIdToken() {
        try {
            var idTokenJwt = SignedJWT.parse(idToken);
            return (String) idTokenJwt.getJWTClaimsSet().getClaims().get("sub");
        } catch (ParseException e) {
            throw new AuthorizationException(500, e.getMessage());
        }
    }
}
