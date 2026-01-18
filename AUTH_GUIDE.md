# Authentication & Role-Based Access Control

## Overview
The application now features a modern login system with role-based dashboards for Admin, Manager, and Customer users.

## Demo Credentials

### Admin Access
- **Email:** admin@shop.com
- **Password:** admin123
- **Capabilities:** Full CRUD operations, user management, category management, view all statistics

### Manager Access
- **Email:** manager@shop.com
- **Password:** manager123
- **Capabilities:** Inventory management, stock updates, view orders, product search

### Customer Access
- **Email:** customer@shop.com
- **Password:** customer123
- **Capabilities:** Browse products, add to cart, checkout, view order history

## Features

### 🔐 Authentication System
- Secure SHA-256 password hashing
- Session management across the application
- Role-based dashboard routing

### 🎨 Modern UI Design
- Clean blue/purple gradient color scheme
- Responsive layouts with modern cards and shadows
- Consistent styling across all pages
- Intuitive navigation and user experience

### 👤 Role-Based Dashboards

#### Admin Dashboard
- Statistics cards (Products, Users, Categories)
- Full product management (Add, Edit, Delete)
- User management access
- Category management access
- Advanced search functionality

#### Manager Dashboard
- Inventory-focused statistics (Low stock alerts)
- Product search and filtering
- Stock update capabilities
- Order management access

#### Customer Dashboard
- Product browsing with search
- Shopping cart functionality
- Real-time cart total calculation
- Checkout process
- Order history access

## Architecture Changes

### New Components
1. **AuthService** - Handles authentication logic
2. **SessionManager** - Maintains user session state
3. **LoginController** - Manages login page
4. **AdminDashboardController** - Admin-specific features
5. **ManagerDashboardController** - Manager-specific features
6. **CustomerDashboardController** - Customer shopping experience

### Security
- Passwords are hashed using SHA-256
- Session-based authentication
- Role validation on dashboard access

## Color Scheme
- **Primary:** #667eea (Blue)
- **Secondary:** #764ba2 (Purple)
- **Success:** #10b981 (Green)
- **Danger:** #ef4444 (Red)
- **Background:** #f3f4f6 (Light Gray)
- **Text:** #1f2937 (Dark Gray)

## Running the Application

1. **Setup Database:**
   ```bash
   mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
   ```

2. **Run Application:**
   ```bash
   mvn javafx:run
   ```

3. **Login** with any of the demo credentials above

## Next Steps
- Implement full user management UI
- Add category management UI
- Create order history view
- Add product image support
- Implement password reset functionality
