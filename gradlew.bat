@rem
@rem Gradle startup script for Windows
@rem

@if "%DEBUG%"=="" @echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

if defined JAVA_HOME (
    set "JAVACMD=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVACMD=java.exe"
)

if not exist "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" (
    where gradle >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        gradle %*
        exit /b %ERRORLEVEL%
    )
)

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
"%JAVACMD%" -Xmx64m -Xms64m %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
