@echo off
echo ==========================================
echo Smart E-Commerce System - Setup
echo ==========================================
echo.

REM Set environment variables (matching docker-compose.yml)
set DB_NAME=ecommerce_db
set POSTGRES_USER=spycon
set POSTGRES_PASSWORD=postgressPassword12345

echo Step 1: Setting up database...
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"

echo.
echo ==========================================
echo Setup Complete!
echo ==========================================
echo.
echo Demo Credentials:
echo   Admin:    admin@shop.com / admin123
echo   Manager:  manager@shop.com / manager123
echo   Customer: customer@shop.com / customer123
echo.
echo Run the application with: mvn javafx:run
echo.
pause
