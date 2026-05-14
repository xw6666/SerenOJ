# Deploy Config

`deploy.bat` reads `deploy\application-prod.yml` and uploads it to:

```text
/root/serenoj/config/application-prod.yml
```

`deploy\application-prod.yml` contains production secrets and is ignored by Git.

Keep the tracked Spring config files free of real passwords. Put real values only in the ignored local deploy config or in server environment variables.

`deploy.bat` now deploys both sides:

1. Push local Git commits to the `oj` remote.
2. Reset `/root/serenoj/project` to `origin/master`.
3. Upload `deploy\application-prod.yml`.
4. Install `nodejs` and `npm` with `apt-get` if they are missing on the Ubuntu server.
5. Run `npm ci --legacy-peer-deps` and `npm run build` in `frontend/`.
6. Copy `frontend/dist` into `src/main/resources/static`.
7. Build the Spring Boot jar and restart it on port `8080`.

The remote server must have `apt-get`, `mvn`, and `java` available in PATH. `node` and `npm` are installed automatically when missing.
The deployed frontend is served by the same Spring Boot process at `http://142.93.85.237:8080/`, and frontend API requests use the same-origin `/api/...` paths.

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
