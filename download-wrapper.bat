@echo off
setlocal

set WRAPPER_DIR=%~dp0gradle\wrapper
if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"

set WRAPPER_JAR=%WRAPPER_DIR%\gradle-wrapper.jar
if exist "%WRAPPER_JAR%" (
  echo gradle-wrapper.jar ja existe.
  exit /b 0
)

set WRAPPER_URL=https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradle/wrapper/gradle-wrapper.jar

echo Baixando gradle-wrapper.jar...
powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"

if exist "%WRAPPER_JAR%" (
  echo Wrapper baixado com sucesso.
  exit /b 0
)

echo Falha ao baixar o wrapper.
exit /b 1
