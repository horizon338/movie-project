@echo off
chcp 65001 >nul
cd /d "%~dp0frontend"
if not exist node_modules (
    echo [1/2] 安装前端依赖...
    call npm install
)
echo [2/2] 启动前端 http://localhost:5173
call npm run dev
pause
