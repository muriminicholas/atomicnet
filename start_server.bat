@echo off
echo Starting Atomicnet Backend...
cd C:\Users\murim\Projects\atomicnet\hotspot-backend
mvn spring-boot:run
if %ERRORLEVEL% neq 0 (
    echo Failed to start backend
    exit /b %ERRORLEVEL%
)
echo Backend running at http://localhost:8081