package jp.classmethod.samplerelyingparty.web;

import java.io.Serializable;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

/** ログインユーザ情報. */
public class LoginUser implements Serializable {

    private static final LoginUser NO_LOGIN_USER = new LoginUser(null);

    private static final String SESSION_KEY = LoginUser.class.getName();

    /** username. */
    private final String username;

    public LoginUser(String username) {
        this.username = username;
    }

    /**
     * Session 上のインスタンス取得.
     *
     * <p>Session 上に存在しない場合、未ログインを示すログインユーザ情報を返却します。
     *
     * @param request request
     * @return ログインユーザ情報
     */
    public static LoginUser fromSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) {
            return NO_LOGIN_USER;
        }
        var loggedInUser = (LoginUser) session.getAttribute(SESSION_KEY);
        return loggedInUser != null ? loggedInUser : NO_LOGIN_USER;
    }

    /**
     * Session へ設定.
     *
     * <p>sessionId の付け替えも行います。
     *
     * @param username username
     * @param request request
     */
    public static void storeSession(String username, HttpServletRequest request) {
        var session = request.getSession(true);
        session.setAttribute(SESSION_KEY, new LoginUser(username));
        SessionRegenerator.regenerateSession(request);
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

    /**
     * ログインしているか？
     *
     * @return ログインしている場合、true
     */
    public boolean isLoggedIn() {
        return Objects.nonNull(username);
    }

    public String getUsername() {
        return username;
    }
}
