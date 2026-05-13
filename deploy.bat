@echo off
chcp 65001 >nul
echo ============================================
echo   SerenOJ Deploy to Remote Server
echo ============================================

set SERVER=oj
set PROJECT_DIR=/root/serenoj/project
set APP_JAR=target/serenoj-1.0.0.jar

echo.
echo [1/4] Pushing code to server...
git push %SERVER% master
if %errorlevel% neq 0 (
    echo [ERROR] git push failed
    pause
    exit /b %errorlevel%
)

echo.
echo [2/4] Pulling code on server...
ssh %SERVER% "cd %PROJECT_DIR% && git fetch origin master && git reset --hard origin/master"
if %errorlevel% neq 0 (
    echo [ERROR] git sync failed
    pause
    exit /b %errorlevel%
)

echo.
echo [3/4] Compiling on server...
ssh %SERVER% "cd %PROJECT_DIR% && mvn -q -DskipTests clean package"
if %errorlevel% neq 0 (
    echo [ERROR] mvn package failed
    pause
    exit /b %errorlevel%
)

echo.
echo [4/4] Kill old process and restart...
ssh %SERVER% "pkill -f '[s]erenoj-1.0.0.jar' 2>/dev/null || true; pkill -f '[s]pring-boot:run' 2>/dev/null || true; pkill -f '[t]op.wjr.serenoj.SerenOJApplication' 2>/dev/null || true"
if %errorlevel% neq 0 (
    echo [ERROR] failed to stop old process
    pause
    exit /b %errorlevel%
)

ssh %SERVER% "bash -lc 'for i in {1..20}; do if ! ss -ltnp | grep -q :8080; then exit 0; fi; sleep 1; done; echo [ERROR] port 8080 is still in use before restart; exit 1'"
if %errorlevel% neq 0 (
    echo [ERROR] port 8080 still in use before restart
    pause
    exit /b %errorlevel%
)

ssh %SERVER% "cd %PROJECT_DIR% && nohup java -jar %APP_JAR% > /tmp/serenoj.log 2>&1 < /dev/null &"
if %errorlevel% neq 0 (
    echo [ERROR] failed to start app
    pause
    exit /b %errorlevel%
)

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
