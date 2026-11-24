@echo off
chcp 65001 >nul
cd /d "%~dp0backend"
echo [1/1] 启动后端 http://localhost:8080
call mvnw.cmd spring-boot:run
pause
