# Path to your project
$projectPath = "C:\Users\murim\projects\atomicnet\hotspot-backend"

# Move to project directory
Set-Location $projectPath

Write-Host "Purging local repository dependencies for hotspot-backend..." -ForegroundColor Yellow
mvn dependency:purge-local-repository -DreResolve=false

Write-Host "Cleaning project..." -ForegroundColor Cyan
mvn clean

Write-Host "Rebuilding project..." -ForegroundColor Green
mvn install -U
Write-Host "Purge and rebuild completed." -ForegroundColor blue