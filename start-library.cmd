@echo off
setlocal
chcp 65001 >nul

set "ROOT=%~dp0"
set "BACKEND_DIR=%ROOT%backend"
set "FRONTEND_DIR=%ROOT%frontend"
set "BACKEND_JAR=%BACKEND_DIR%\target\backend-0.1.0-SNAPSHOT.jar"
set "FRONTEND_JAR=%FRONTEND_DIR%\target\frontend-0.1.0-SNAPSHOT.jar"

if not exist "%BACKEND_JAR%" (
  echo [ERROR] Backend JAR not found: %BACKEND_JAR%
  echo Run mvnw.cmd clean package in backend first.
  pause
  exit /b 1
)

if not exist "%FRONTEND_JAR%" (
  echo [ERROR] Frontend JAR not found: %FRONTEND_JAR%
  echo Run mvnw.cmd clean package in frontend first.
  pause
  exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Java not found. Please install JDK and add it to PATH.
  pause
  exit /b 1
)

echo [1/2] Starting backend service...
start "library-backend" cmd /k "cd /d ""%BACKEND_DIR%"" && java -jar target\backend-0.1.0-SNAPSHOT.jar"

echo [2/2] Waiting for backend, then starting frontend...
timeout /t 8 /nobreak >nul
start "library-frontend" cmd /k "cd /d ""%FRONTEND_DIR%"" && java -Dapi.baseUrl=http://127.0.0.1:8081 -jar target\frontend-0.1.0-SNAPSHOT.jar"

echo.
echo Started:
echo - Backend: http://127.0.0.1:8081
echo - Frontend: JavaFX client window
echo.
echo Demo accounts:
echo - Admin: admin / 123456
echo - Staff: staff01 / 123456
echo - Reader: reader01 / 123456
echo - Reader: reader02 / 123456
if /i not "%~1"=="--no-pause" pause
