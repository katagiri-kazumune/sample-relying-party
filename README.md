# 事前準備

ローカルに barista を構築します。

## Client 登録

barista に Client を登録します。
(その Client の redirect_uri に

* `http://localhost:8888/`
* `http://localhost:8888/oauth/callback`
* `http://localhost:8888/mfa-config/callback`

を登録しておいてください)

## ユーザー登録

事前にユーザーを登録しておきます。
MFA 設定画面を表示する権限として `BARISTA_MFA_CONFIGURABLE` を設定してください。

## barista 設定

barista で MFA が有効な設定しておきます。
(`barista.mfa.email-enabled=true` 等にしてください)

DoSignup 後に実行するアクション(`barista.signup.post-signup-action`) に、
`http://localhost:8888/signup/callback`
を設定します。

DoSignup 時に新規ユーザーに付与する権限(`barista.signup.signup-authorities`) に、
`BARISTA_MFA_CONFIGURABLE`
を追加設定します(これをしないと signup 直後に MFA 設定画面に遷移できません)。

MFA 設定画面(`mfa-registration.html`) に以下を設定します

```js
<script type="text/javascript" th:inline="javascript" th:if="${emailRegistered}">
	window.onload = function() {
		const redirectUri = "[(${session[T(jp.classmethod.aws.barista.web.utils.ClientIdDetectionFilter).REDIRECT_URI] ?: ''})]";
		if(redirectUri !== '') {
			setTimeout(function(){location.href = redirectUri}, 1500);
		}
	};
</script>
```

## barista 起動

barista を起動します。

# 実行

以下のコマンドを実行してください。

```shell script
export BARISTA_AUTHORIZE_AUTHORIZATION_ENDPOINT=${認可エンドポイント URI(e.g. http://localhost:8080/oauth/authorize)}
export BARISTA_AUTHORIZE_CLIENT_ID=${認可エンドポイントで使用する client_id}
export BARISTA_AUTHORIZE_CLIENT_SECRET=${認可エンドポイントで使用する client の secret}
export BARISTA_AUTHORIZE_TOKEN_ENDPOINT=${Token エンドポイント URI(e.g. http://localhost:8080/oauth/token)}
export BARISTA_API_GET_USER_ENDPOINT=${GetUser エンドポイント URI(e.g. http://localhost:8080/users/%s)}
export BARISTA_UI_GET_MFA_REGISTRATION_FORM_ENDPOINT=${GetMfaRegistrationForm エンドポイント URI(e.g. http://localhost:8080/mfa-config)}

./mvnw compile quarkus:dev
```

ブラウザで `http://localhost:8888` にアクセスすると参照できます。

ユーザーに EMAIL の MFA 設定がある時にログイン状態になります。
(EMAIL の MFA 設定が無いと MFA 設定画面に遷移します)

# フォーマットする時

```sh
./mvnw spotless:apply
```

よろしく
