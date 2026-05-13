@echo off
chcp 65001 >nul
echo ============================================
echo   SerenOJ Deploy to Remote Server
echo ============================================

set SERVER=oj
set PROJECT_DIR=/root/serenoj/project
set APP_JAR=target/serenoj-1.0.0.jar
set LOCAL_CONFIG=deploy\application-prod.yml
set REMOTE_CONFIG_DIR=/root/serenoj/config

if not exist "%LOCAL_CONFIG%" (
    echo [ERROR] Missing local deploy config: %LOCAL_CONFIG%
    echo See deploy\README.md, then create the ignored local production config.
    pause
    exit /b 1
)

echo.
echo [1/5] Pushing code to server...
git push %SERVER% master
if %errorlevel% neq 0 (
    echo [ERROR] git push failed
    pause
    exit /b %errorlevel%
)

echo.
echo [2/5] Pulling code on server...
ssh %SERVER% "cd %PROJECT_DIR% && git fetch origin master && git reset --hard origin/master"
if %errorlevel% neq 0 (
    echo [ERROR] git sync failed
    pause
    exit /b %errorlevel%
)

echo.
echo [3/5] Syncing production config...
ssh %SERVER% "mkdir -p %REMOTE_CONFIG_DIR% && chmod 700 %REMOTE_CONFIG_DIR%"
if %errorlevel% neq 0 (
    echo [ERROR] failed to create remote config directory
    pause
    exit /b %errorlevel%
)

scp "%LOCAL_CONFIG%" %SERVER%:%REMOTE_CONFIG_DIR%/application-prod.yml
if %errorlevel% neq 0 (
    echo [ERROR] failed to sync production config
    pause
    exit /b %errorlevel%
)

echo.
echo [4/5] Compiling on server...
ssh %SERVER% "cd %PROJECT_DIR% && mvn -q -DskipTests clean package"
if %errorlevel% neq 0 (
    echo [ERROR] mvn package failed
    pause
    exit /b %errorlevel%
)

echo.
echo [5/5] Kill old process and restart...
echo Stopping old app...
ssh %SERVER% "pkill -f '[s]erenoj-1.0.0.jar' 2>/dev/null || true; pkill -f '[s]pring-boot:run' 2>/dev/null || true; pkill -f '[t]op.wjr.serenoj.SerenOJApplication' 2>/dev/null || true"
if %errorlevel% neq 0 (
    echo [ERROR] failed to stop old process
    pause
    exit /b %errorlevel%
)

echo Waiting for port 8080 to be released...
ssh %SERVER% "bash -lc 'for i in {1..20}; do if ! ss -ltnp | grep -q :8080; then exit 0; fi; sleep 1; done; echo [ERROR] port 8080 is still in use before restart; exit 1'"
if %errorlevel% neq 0 (
    echo [ERROR] port 8080 still in use before restart
    pause
    exit /b %errorlevel%
)

echo Starting new app...
ssh %SERVER% "cd %PROJECT_DIR% && setsid -f java -jar %APP_JAR% --spring.profiles.active=prod --spring.config.additional-location=file:%REMOTE_CONFIG_DIR%/ > /tmp/serenoj.log 2>&1 < /dev/null"
if %errorlevel% neq 0 (
    echo [ERROR] failed to start app
    pause
    exit /b %errorlevel%
)

echo Waiting for app to listen on 8080...
ssh %SERVER% "bash -lc 'for i in {1..30}; do if ss -ltnp | grep -q :8080; then exit 0; fi; sleep 1; done; echo [ERROR] app did not listen on 8080; tail -n 50 /tmp/serenoj.log; exit 1'"
if %errorlevel% neq 0 (
    echo [ERROR] app startup check failed
    pause
    exit /b %errorlevel%
)

echo.
echo ============================================
echo   Deploy complete!
echo   App running on http://142.93.85.237:8080
echo   View logs: ssh %SERVER% "tail -f /tmp/serenoj.log"
echo ============================================
pause
