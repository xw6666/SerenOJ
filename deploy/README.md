# Deploy Config

`deploy.bat` reads `deploy\application-prod.yml` and uploads it to:

```text
/root/serenoj/config/application-prod.yml
```

`deploy\application-prod.yml` contains production secrets and is ignored by Git.

Keep the tracked Spring config files free of real passwords. Put real values only in the ignored local deploy config or in server environment variables.

Minimal shape:

```yaml
spring:
  datasource:
    username: root
    password: "<database-password>"
  redis:
    password: "<redis-password>"

serenoj:
  jwt:
    secret: "<long-random-secret>"
```
