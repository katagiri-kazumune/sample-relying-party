package jp.classmethod.samplerelyingparty.web.oauth;

import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import jp.classmethod.samplerelyingparty.exception.AuthorizationException;

public class AuthorizationCodeGrantResponse {

    private final String accessToken;

    private final String idToken;

    private final String refreshToken;

    public AuthorizationCodeGrantResponse(String accessToken, String idToken, String refreshToken) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        if (idToken != null) {
            return getUsernameForIdToken();
        }
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
