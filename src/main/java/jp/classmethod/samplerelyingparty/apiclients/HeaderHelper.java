package jp.classmethod.samplerelyingparty.apiclients;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

public class HeaderHelper {

    public static String buildAuthorizationValue(String username, String password) {
        return String.format(
                Locale.ENGLISH,
                "Basic %s",
                Base64.getEncoder()
                        .encodeToString(
                                (username + ":" + password).getBytes(StandardCharsets.UTF_8)));
    }
}
