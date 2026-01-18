# UI Flow & Screenshots Guide

## Application Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      LOGIN PAGE                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              🛒 Smart E-Commerce                      │  │
│  │         Sign in to your account                       │  │
│  │                                                        │  │
│  │  Email: [____________________]                        │  │
│  │  Password: [____________________]                     │  │
│  │                                                        │  │
│  │           [    Sign In    ]                           │  │
│  │                                                        │  │
│  │  Demo Credentials:                                    │  │
│  │  • admin@shop.com / admin123                         │  │
│  │  • manager@shop.com / manager123                     │  │
│  │  • customer@shop.com / customer123                   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
        ┌───────▼────────┐    ┌────────▼────────┐
        │  Role: Admin   │    │  Role: Manager  │
        └───────┬────────┘    └────────┬────────┘
                │                      │
                │              ┌───────▼────────┐
                │              │ Role: Customer │
                │              └───────┬────────┘
                │                      │
                ▼                      ▼
```

## Admin Dashboard Layout

```
┌─────────────────────────────────────────────────────────────┐
│ 🛒 Admin Dashboard          Welcome, Admin User   [Logout]  │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Total        │  │ Total        │  │ Categories   │     │
│  │ Products     │  │ Users        │  │              │     │
│  │    50        │  │    8         │  │    8         │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
├─────────────────────────────────────────────────────────────┤
│  Search: [________________]  [Search]  [+ Add Product]     │
├─────────────────────────────────────────────────────────────┤
│  Product Management                                         │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ ID │ Product Name        │ Price    │ Stock          │ │
│  ├────┼────────────────────┼──────────┼────────────────┤ │
│  │ 1  │ Laptop Pro 15      │ $1299.99 │ 25             │ │
│  │ 2  │ Smartphone X12     │ $899.99  │ 50             │ │
│  │ 3  │ Wireless Earbuds   │ $199.99  │ 100            │ │
│  └───────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  [Edit] [Delete] [Manage Users] [Manage Categories]        │
└─────────────────────────────────────────────────────────────┘
```

## Manager Dashboard Layout

```
┌─────────────────────────────────────────────────────────────┐
│ 📦 Manager Dashboard      Welcome, Store Manager  [Logout]  │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐  ┌──────────────────┐               │
│  │ Low Stock Items  │  │ Total Products   │               │
│  │       5          │  │       50         │               │
│  └──────────────────┘  └──────────────────┘               │
├─────────────────────────────────────────────────────────────┤
│  Search: [________________]  [Search]  [View Orders]       │
├─────────────────────────────────────────────────────────────┤
│  Inventory Management                                       │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ ID │ Product Name        │ Price    │ Stock          │ │
│  ├────┼────────────────────┼──────────┼────────────────┤ │
│  │ 1  │ Laptop Pro 15      │ $1299.99 │ 25             │ │
│  │ 2  │ Smartphone X12     │ $899.99  │ 50             │ │
│  │ 15 │ Cotton T-Shirt     │ $19.99   │ 8 ⚠️          │ │
│  └───────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                                        [Update Stock]       │
└─────────────────────────────────────────────────────────────┘
```

## Customer Dashboard Layout

```
┌─────────────────────────────────────────────────────────────────────┐
│ 🛍️ Shop Now    Welcome, John Doe  [My Orders] [Logout]            │
├─────────────────────────────────────────────────────────────────────┤
│  Search: [_______________________]  [Search]                       │
├──────────────────────────────────────┬──────────────────────────────┤
│  Available Products                  │  🛒 Shopping Cart            │
│  ┌────────────────────────────────┐  │  ┌────────────────────────┐ │
│  │ ID │ Name      │ Price │ Stock │  │  │ Item │ Qty │ Price    │ │
│  ├────┼───────────┼───────┼───────┤  │  ├──────┼─────┼──────────┤ │
│  │ 1  │ Laptop    │ $1299 │ 25    │  │  │ Phone│  1  │ $899.99  │ │
│  │ 2  │ Phone     │ $899  │ 50    │  │  │ Buds │  2  │ $399.98  │ │
│  │ 3  │ Earbuds   │ $199  │ 100   │  │  └──────┴─────┴──────────┘ │
│  │ 4  │ Watch     │ $349  │ 40    │  │                             │
│  └────────────────────────────────┘  │  Total: $1,299.97           │
│                                       │                             │
│  [Add to Cart]                       │  [Remove Selected]          │
│                                       │  [Checkout]                 │
└──────────────────────────────────────┴──────────────────────────────┘
```

## Color Palette

### Primary Colors
- **Blue (#667eea)** - Primary buttons, headers, branding
- **Purple (#764ba2)** - Gradients, accents
- **White (#ffffff)** - Cards, backgrounds

### Action Colors
- **Green (#10b981)** - Success, Add, Checkout
- **Red (#ef4444)** - Delete, Logout, Danger
- **Blue (#3b82f6)** - Edit, Info

### Neutral Colors
- **Light Gray (#f3f4f6)** - Page background
- **Gray (#6b7280)** - Secondary text
- **Dark Gray (#1f2937)** - Primary text
- **Border (#d1d5db)** - Input borders

## Design Principles

1. **Consistency** - Same layout pattern across all dashboards
2. **Clarity** - Clear visual hierarchy with cards and sections
3. **Accessibility** - High contrast, readable fonts
4. **Responsiveness** - Proper spacing and alignment
5. **Modern** - Gradients, shadows, rounded corners
6. **Intuitive** - Familiar e-commerce patterns

## Key UI Elements

### Cards with Gradients
```
┌─────────────────────────┐
│ Total Products          │  ← White text
│                         │  ← Gradient background
│       50                │  ← Large number
└─────────────────────────┘
```

### Modern Buttons
```
┌──────────────┐
│   Sign In    │  ← Bold text, rounded corners
└──────────────┘  ← Hover effect, cursor pointer
```

### Clean Tables
```
┌────┬──────────┬────────┐
│ ID │ Name     │ Price  │  ← Bold headers
├────┼──────────┼────────┤
│ 1  │ Product  │ $99.99 │  ← Alternating rows
└────┴──────────┴────────┘
```

### Search Bars
```
┌─────────────────────────┐  ┌──────────┐
│ Search products...      │  │  Search  │
└─────────────────────────┘  └──────────┘
```

## User Experience Flow

1. **Login** → Enter credentials → Validate → Route by role
2. **Admin** → View stats → Manage products/users/categories
3. **Manager** → Check inventory → Update stock → View orders
4. **Customer** → Browse → Add to cart → Checkout → Order placed
5. **Logout** → Clear session → Return to login

## Responsive Design

- Minimum window size: 1000x700
- Tables adjust to content
- Cards stack vertically on smaller screens
- Consistent padding and margins
- Proper use of HBox.hgrow and VBox.vgrow

---

**Note:** This is a desktop application optimized for 1000x700 resolution and above.
