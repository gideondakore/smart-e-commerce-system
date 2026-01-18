# Quick Reference Card

## 🚀 Getting Started (3 Steps)

```bash
# 1. Start PostgreSQL (if using Docker)
docker-compose up -d

# 2. Setup Database (Automatic!)
./setup.sh

# 3. Run Application
mvn javafx:run
```

## 📋 Common Commands

### Database Setup
```bash
# Linux/Mac
./setup.sh

# Windows
setup.bat

# Manual
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
```

### Run Application
```bash
mvn javafx:run
```

### Build Project
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

### Performance Test
```bash
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.PerformanceTestRunner"
```

## 🔐 Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@shop.com | admin123 |
| Manager | manager@shop.com | manager123 |
| Customer | customer@shop.com | customer123 |

## 🗄️ Database Info

```
Host: localhost
Port: 5432
Database: ecommerce_db
User: spycon
Password: postgressPassword12345
```

## 🐳 Docker Commands

```bash
# Start PostgreSQL
docker-compose up -d

# Stop PostgreSQL
docker-compose down

# View logs
docker-compose logs -f

# Access PostgreSQL CLI
docker exec -it smart-ecommerce psql -U spycon -d ecommerce_db

# Access pgAdmin
# Open browser: http://localhost:5050
# Email: admin@admin.com
# Password: admin
```

## 🔧 Environment Variables

```bash
# Linux/Mac
export DB_NAME=ecommerce_db
export POSTGRES_USER=spycon
export POSTGRES_PASSWORD=postgressPassword12345

# Windows
set DB_NAME=ecommerce_db
set POSTGRES_USER=spycon
set POSTGRES_PASSWORD=postgressPassword12345
```

## 📁 Important Files

```
src/main/resources/
├── fxml/
│   ├── login.fxml              # Login page
│   ├── admin-dashboard.fxml    # Admin interface
│   ├── manager-dashboard.fxml  # Manager interface
│   └── customer-dashboard.fxml # Customer interface
├── sql/
│   ├── schema.sql              # Database schema
│   └── seed_data.sql           # Demo data
└── styles/
    └── application.css         # Global styles
```

## 🎨 Color Palette

```
Primary:   #667eea (Blue)
Secondary: #764ba2 (Purple)
Success:   #10b981 (Green)
Danger:    #ef4444 (Red)
Info:      #3b82f6 (Blue)
```

## 🔍 Useful SQL Queries

```sql
-- View all users
SELECT * FROM users;

-- View all products
SELECT * FROM products;

-- View orders with user info
SELECT o.*, u.email FROM orders o JOIN users u ON o.user_id = u.user_id;

-- Check low stock items
SELECT * FROM products WHERE stock_quantity < 10;

-- View product reviews
SELECT p.name, r.rating, r.comment FROM reviews r JOIN products p ON r.product_id = p.product_id;
```

## 🐛 Troubleshooting

### Database Connection Failed
```bash
# Check PostgreSQL is running
docker-compose ps
# or
sudo systemctl status postgresql
```

### Port Already in Use
```bash
# Check what's using port 5432
sudo lsof -i :5432
# or
netstat -ano | findstr :5432
```

### Reset Everything
```bash
# Stop containers
docker-compose down -v

# Remove database
docker volume rm smart-e-commerce-system_postgres_data

# Start fresh
docker-compose up -d
./setup.sh
```

## 📚 Documentation

- [README.md](README.md) - Main documentation
- [AUTH_GUIDE.md](AUTH_GUIDE.md) - Authentication details
- [DATABASE_SETUP.md](DATABASE_SETUP.md) - Database setup guide
- [CHANGES.md](CHANGES.md) - Complete changelog
- [UI_GUIDE.md](UI_GUIDE.md) - UI design guide

## 💡 Tips

1. **First Time Setup**: Run `./setup.sh` before starting the app
2. **Database Reset**: Drop database and run setup again
3. **Custom Config**: Use environment variables
4. **Docker**: Easiest way to run PostgreSQL
5. **pgAdmin**: Web UI for database management at localhost:5050

## 🎯 Project Structure

```
smart-e-commerce-system/
├── src/main/java/com/amalitech/smartecommerce/
│   ├── app/           # Main application
│   ├── controllers/   # UI controllers
│   ├── dao/          # Data access objects
│   ├── models/       # Domain models
│   ├── services/     # Business logic
│   └── utils/        # Utilities
├── src/main/resources/
│   ├── fxml/         # UI layouts
│   ├── sql/          # Database scripts
│   └── styles/       # CSS files
├── setup.sh          # Linux/Mac setup
├── setup.bat         # Windows setup
└── docker-compose.yml # Docker config
```

---

**Keep this card handy for quick reference! 📌**
