@echo off
setlocal
chcp 65001 >nul

set "ROOT=%~dp0"
set "SCHEMA=%ROOT%sql\library_management_schema.sql"
set "SEED=%ROOT%sql\library_management_seed.sql"
set "MYSQL_EXE=mysql"
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=library_management"
set "DB_USER=root"
set "DB_PASSWORD=root"

if not exist "%SCHEMA%" (
  echo [ERROR] SQL file not found: %SCHEMA%
  pause
  exit /b 1
)

where "%MYSQL_EXE%" >nul 2>&1
if errorlevel 1 (
  if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
  if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
  if exist "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe"
)

if not exist "%MYSQL_EXE%" if /i not "%MYSQL_EXE%"=="mysql" (
  echo [ERROR] mysql.exe not found. Install MySQL client or add it to PATH.
  pause
  exit /b 1
)

echo [1/2] Rebuilding database schema...
"%MYSQL_EXE%" --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% < "%SCHEMA%"
if errorlevel 1 (
  echo [ERROR] Failed to apply schema SQL. Check MySQL service and credentials.
  pause
  exit /b 1
)

echo [2/2] Importing demo data...
"%MYSQL_EXE%" --default-character-set=utf8mb4 -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% < "%SEED%"
if errorlevel 1 (
  echo [ERROR] Failed to apply seed SQL. Check MySQL service and credentials.
  pause
  exit /b 1
)

echo.
echo Demo data reset completed:
echo - Database: %DB_NAME%
echo - Admin: admin / 123456
echo - Staff: staff01 / 123456, staff02 / 123456
echo - Readers: reader01 ~ reader08 / 123456
if /i not "%~1"=="--no-pause" pause
