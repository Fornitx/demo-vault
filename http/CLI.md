```sh
vault kv get secret/myapp
```
```sh
vault kv put secret/myapp password='pass123'
```
```sh
vault kv list secret/
```
```sh
vault kv get -output-curl-string secret/myapp
```