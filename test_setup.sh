#!/bin/bash
echo "Testing database setup automation..."
echo ""
echo "Configuration:"
echo "  DB_NAME: ${DB_NAME:-ecommerce_db}"
echo "  POSTGRES_USER: ${POSTGRES_USER:-spycon}"
echo "  Host: localhost:5432"
echo ""
echo "This will test the automated database creation."
echo "Press Ctrl+C to cancel, or Enter to continue..."
read

export DB_NAME=ecommerce_db
export POSTGRES_USER=spycon
export POSTGRES_PASSWORD=postgressPassword12345

mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
