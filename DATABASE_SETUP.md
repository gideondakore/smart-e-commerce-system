# Database Setup Guide

## Automated Database Creation

The Smart E-Commerce System now features **fully automated database setup**! No manual database creation required.

## How It Works

### 1. Automatic Database Creation
When you run the setup, the system will:
- Connect to PostgreSQL server (default: localhost:5432)
- Check if `ecommerce_db` database exists
- Create the database if it doesn't exist
- Create all necessary tables
- Insert demo data with proper password hashing

### 2. Default Configuration

The system uses these default values (matching docker-compose.yml):

```
Database Name: ecommerce_db
Username: spycon
Password: postgressPassword12345
Host: localhost
Port: 5432
```

### 3. Environment Variables (Optional)

You can override defaults using environment variables:

**Linux/Mac:**
```bash
export DB_NAME=ecommerce_db
export POSTGRES_USER=spycon
export POSTGRES_PASSWORD=postgressPassword12345
```

**Windows:**
```cmd
set DB_NAME=ecommerce_db
set POSTGRES_USER=spycon
set POSTGRES_PASSWORD=postgressPassword12345
```

## Quick Start

### Option 1: Using Setup Scripts (Easiest)

**Linux/Mac:**
```bash
chmod +x setup.sh
./setup.sh
```

**Windows:**
```cmd
setup.bat
```

### Option 2: Using Maven Directly

```bash
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
```

### Option 3: Using Docker Compose

```bash
# Start PostgreSQL with Docker
docker-compose up -d

# Wait a few seconds for PostgreSQL to start
sleep 5

# Run setup
./setup.sh
```

## What Gets Created

### Database Structure

```
ecommerce_db
├── users (8 demo users)
│   ├── admin@shop.com (admin)
│   ├── manager@shop.com (manager)
│   └── customer@shop.com (customer)
│   └── 5 more customers
├── categories (8 categories)
│   ├── Electronics
│   ├── Books
│   ├── Clothing
│   └── 5 more categories
├── products (50+ products)
├── orders (6 sample orders)
├── order_items (order details)
├── reviews (10 product reviews)
└── inventory_logs (8 stock changes)
```

### Tables Created

1. **users** - User accounts with roles (admin, manager, customer)
2. **categories** - Product categories
3. **products** - Product catalog with pricing and stock
4. **orders** - Customer orders
5. **order_items** - Order line items
6. **reviews** - Product reviews and ratings
7. **inventory_logs** - Stock change history

### Indexes Created

For optimal performance:
- Product name search index
- User email index
- Order date index
- Category lookup index
- And more...

## Verification

After setup completes, you'll see:

```
✓ Database 'ecommerce_db' created successfully!
✓ Schema created successfully!
✓ Seed data inserted successfully!

🔍 Verifying database setup...

  Table                   Row Count
  ────────────────────────────────────────
  users                   8
  categories              8
  products                50
  orders                  6
  order_items             11
  reviews                 10
  inventory_logs          8

✅ Database setup completed successfully!
```

## Troubleshooting

### Issue: "Connection refused"
**Solution:** Ensure PostgreSQL is running
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql

# Or with Docker
docker-compose ps
```

### Issue: "Database already exists"
**Solution:** This is normal! The setup will use the existing database and recreate tables.

### Issue: "Authentication failed"
**Solution:** Check your PostgreSQL credentials
```bash
# Test connection
psql -h localhost -U spycon -d postgres
```

### Issue: "Permission denied"
**Solution:** Ensure your PostgreSQL user has CREATE DATABASE permission
```sql
-- As postgres superuser
ALTER USER spycon CREATEDB;
```

## Reset Database

To completely reset the database:

```bash
# Drop the database
psql -h localhost -U spycon -d postgres -c "DROP DATABASE IF EXISTS ecommerce_db;"

# Run setup again
./setup.sh
```

## Custom Configuration

### Using Different Database Name

```bash
export DB_NAME=my_custom_db
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
```

### Using Different Credentials

```bash
export POSTGRES_USER=myuser
export POSTGRES_PASSWORD=mypassword
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
```

## Security Notes

1. **Password Hashing**: All user passwords are hashed using SHA-256
2. **Demo Passwords**: 
   - admin123 → 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
   - manager123 → 6ee4a469cd4e91053847f5d3fcb61dbcc91e8f0ef10be7748da4c4a1ba382d17
   - customer123 → 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92

3. **Production**: Change default credentials before deploying to production!

## Next Steps

After successful setup:

1. Run the application:
   ```bash
   mvn javafx:run
   ```

2. Login with demo credentials:
   - Admin: admin@shop.com / admin123
   - Manager: manager@shop.com / manager123
   - Customer: customer@shop.com / customer123

3. Explore the role-based dashboards!

## Support

If you encounter issues:
1. Check PostgreSQL is running
2. Verify credentials match docker-compose.yml
3. Check logs for detailed error messages
4. Ensure Java 21+ and Maven are installed

---

**The database setup is now fully automated! Just run the setup script and start coding! 🚀**
