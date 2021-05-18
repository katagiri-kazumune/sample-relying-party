# 事前準備

ローカルに barista を構築します。

barista に Client を登録します。
(その Client の redirect_uri に

* `http://localhost:8888/oauth/callback`
* `http://localhost:8888/mfa-config/callback`

を登録しておいてください)

事前にユーザーを登録しておきます。
MFA 設定画面を表示する権限として `BARISTA_MFA_CONFIGURABLE` を設定してください。

barista で MFA が有効な設定しておきます。
(`barista.mfa.email-enabled=true` 等にしてください)

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

# フォーマット

```sh
./mvnw spotless:apply
```
