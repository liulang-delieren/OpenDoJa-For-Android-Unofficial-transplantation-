@echo off
set "JAVA_HOME=D:\Android Studio\jbr"
set "GRADLE_USER_HOME=D:\gradle-home"
set "TEMP=D:\temp-g"
set "TMP=D:\temp-g"
cd /d %~dp0
call "D:\gradle-home\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat" assembleDebug --no-daemon --console=plain > build.log 2>&1
echo EXIT=%ERRORLEVEL% >> build.log