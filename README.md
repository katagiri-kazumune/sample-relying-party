# 事前準備

ローカルに barista を起動します。

barista に Client を登録します。
(その Client の redirect_uri に `http://localhost:8888/oauth/callback` を登録しておいてください)

事前にユーザーを登録しておきます。

# 実行

以下のコマンドを実行してください。

```shell script
export BARISTA_AUTHORIZE_AUTHORIZATION_ENDPOINT=${認可エンドポイント URI(e.g. http://localhost:8080/oauth/authorize)}
export BARISTA_AUTHORIZE_CLIENT_ID=${認可エンドポイントで使用する client_id}
export BARISTA_AUTHORIZE_CLIENT_SECRET=${認可エンドポイントで使用する client の secret}
export BARISTA_AUTHORIZE_TOKEN_ENDPOINT=${Token エンドポイント URI(e.g. http://localhost:8080/oauth/token)}

./mvnw compile quarkus:dev
```

ブラウザで `http://localhost:8888` にアクセスすると参照できます。

# フォーマット

```sh
./mvnw spotless:apply
```
