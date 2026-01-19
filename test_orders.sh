#!/bin/bash

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║     Order Management - Testing Guide                        ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== WHAT'S NEW ===${NC}"
echo ""
echo "✅ Fixed: 'Remove Selected' button in Customer Dashboard"
echo "✅ New: Customer can view order history"
echo "✅ New: Admin can view all orders"
echo "✅ New: Admin can update order status"
echo ""

echo -e "${BLUE}=== DATABASE STATUS ===${NC}"
echo ""
echo "Current orders in database:"
docker exec -i smart-ecommerce psql -U spycon -d ecommerce_db -c "SELECT order_id, user_id, status, total_amount FROM orders;" 2>/dev/null
echo ""

echo -e "${BLUE}=== TEST SCENARIOS ===${NC}"
echo ""
echo -e "${GREEN}1. Test Customer Dashboard (customer@shop.com / customer123)${NC}"
echo "   a. Add products to cart"
echo "   b. Click 'Remove Selected' without selecting → Should show warning"
echo "   c. Select item and click 'Remove Selected' → Item removed"
echo "   d. Click 'My Orders' → View order history"
echo ""

echo -e "${GREEN}2. Test Admin Dashboard (admin@shop.com / admin123)${NC}"
echo "   a. Click 'Manage Orders' button (bottom right)"
echo "   b. View all orders with customer emails"
echo "   c. Select an order"
echo "   d. Click 'Update Status'"
echo "   e. Choose new status (pending/processing/shipped/delivered/cancelled)"
echo "   f. Verify status updated"
echo ""

echo -e "${BLUE}=== STARTING APPLICATION ===${NC}"
echo ""
echo "Starting JavaFX application..."
echo ""

mvn javafx:run
