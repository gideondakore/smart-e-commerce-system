#!/bin/bash

# Quick Start Script for Smart E-Commerce System
# This script will start the JavaFX application

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║        Smart E-Commerce System - Quick Start                ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Check if Docker is running
echo "🔍 Checking Docker container..."
if ! docker ps | grep -q smart-ecommerce; then
    echo "⚠️  Docker container not running. Starting..."
    docker-compose up -d
    sleep 3
fi

# Verify database connection
echo "🔌 Verifying database connection..."
if ! docker exec -i smart-ecommerce psql -U spycon -d ecommerce_db -c "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ Database connection failed!"
    echo "Please run: docker-compose up -d"
    exit 1
fi

echo "✅ Database connected successfully!"
echo ""
echo "🚀 Starting Smart E-Commerce Application..."
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "                    DEMO CREDENTIALS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "👨‍💼 ADMIN (Full Access):"
echo "   Email:    admin@shop.com"
echo "   Password: admin123"
echo ""
echo "👔 MANAGER (Inventory Management):"
echo "   Email:    manager@shop.com"
echo "   Password: manager123"
echo ""
echo "🛒 CUSTOMER (Shopping):"
echo "   Email:    customer@shop.com"
echo "   Password: customer123"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "⏳ Launching application (this may take a moment)..."
echo ""

# Run the application
mvn javafx:run
