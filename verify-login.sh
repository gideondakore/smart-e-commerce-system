#!/bin/bash

echo "=========================================="
echo "Smart E-Commerce Login Verification"
echo "=========================================="
echo ""

# Check Docker container
echo "1. Checking Docker container..."
if docker ps | grep -q smart-ecommerce; then
    echo "   ✓ Docker container 'smart-ecommerce' is running"
else
    echo "   ✗ Docker container 'smart-ecommerce' is NOT running"
    echo "   Please start the container first"
    exit 1
fi
echo ""

# Check database connection
echo "2. Checking database connection..."
if docker exec -i smart-ecommerce psql -U spycon -d ecommerce_db -c "\dt" > /dev/null 2>&1; then
    echo "   ✓ Database connection successful"
else
    echo "   ✗ Cannot connect to database"
    exit 1
fi
echo ""

# Check users table
echo "3. Checking users in database..."
docker exec -i smart-ecommerce psql -U spycon -d ecommerce_db -c "SELECT email, role FROM users WHERE role IN ('admin', 'manager', 'customer') ORDER BY user_id LIMIT 3;" 2>/dev/null
echo ""

# Verify password hashes
echo "4. Verifying password hashes..."
docker exec -i smart-ecommerce psql -U spycon -d ecommerce_db -c "
SELECT 
    email,
    CASE 
        WHEN password_hash = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9' THEN '✓ admin123'
        WHEN password_hash = '866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5' THEN '✓ manager123'
        WHEN password_hash = 'b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6' THEN '✓ customer123'
        ELSE '✗ Unknown'
    END as password_status
FROM users 
WHERE email IN ('admin@shop.com', 'manager@shop.com', 'customer@shop.com')
ORDER BY user_id;
" 2>/dev/null
echo ""

# Run authentication test
echo "5. Running authentication test..."
mvn -q test-compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.test.LoginFlowTest" -Dexec.classpathScope=test 2>/dev/null | grep -A 50 "Testing Login"
echo ""

echo "=========================================="
echo "Verification Complete!"
echo "=========================================="
echo ""
echo "To run the application:"
echo "  mvn javafx:run"
echo ""
echo "Test credentials:"
echo "  Admin:    admin@shop.com / admin123"
echo "  Manager:  manager@shop.com / manager123"
echo "  Customer: customer@shop.com / customer123"
echo ""
