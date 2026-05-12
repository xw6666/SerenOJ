@echo off
chcp 65001 >nul
echo ============================================
echo   SerenOJ Deploy to Remote Server
echo ============================================

set SERVER=oj
set PROJECT_DIR=/root/serenoj/project

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
ssh %SERVER% "cd %PROJECT_DIR% && git pull origin master"
if %errorlevel% neq 0 (
    echo [ERROR] git pull failed
    pause
    exit /b %errorlevel%
)

echo.
echo [3/4] Compiling on server...
ssh %SERVER% "cd %PROJECT_DIR% && mvn clean compile -q"
if %errorlevel% neq 0 (
    echo [ERROR] mvn compile failed
    pause
    exit /b %errorlevel%
)

echo.
echo [4/4] Kill old process and restart...
ssh %SERVER% "pkill -f 'serenoj-1.0.0.jar' 2>/dev/null; pkill -f 'spring-boot:run' 2>/dev/null; sleep 1; cd %PROJECT_DIR% && nohup mvn spring-boot:run > /tmp/serenoj.log 2>&1 &"

echo.
echo ============================================
echo   Deploy complete!
echo   App running on http://142.93.85.237:8080
echo   View logs: ssh %SERVER% "tail -f /tmp/serenoj.log"
echo ============================================
pause
