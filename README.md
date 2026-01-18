# Smart E-Commerce System

A JavaFX Desktop Application backed by a PostgreSQL database, demonstrating raw JDBC persistence, in-memory caching, algorithmic optimization, and role-based access control.

## Features
- **🔐 Authentication System**: Secure login with SHA-256 password hashing
- **👥 Role-Based Access**: Separate dashboards for Admin, Manager, and Customer
- **🎨 Modern UI**: Clean blue/purple gradient design with consistent styling
- **CRUD Operations**: Manage products and categories
- **Search**: Efficient product search with SQL Indexing
- **Caching**: In-memory `HashMap` cache to reduce database hits
- **Sorting**: Sort products by price (Ascending/Descending)
- **🛒 Shopping Cart**: Real-time cart management and checkout
- **Performance Tracking**: Real-time execution time logging

## Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@shop.com | admin123 |
| Manager | manager@shop.com | manager123 |
| Customer | customer@shop.com | customer123 |

## Prerequisites
- Java 21+
- Maven
- PostgreSQL Server (running on localhost:5432)

## Setup Instructions

### Automated Setup (Recommended)

The application now automatically creates the database and all tables if they don't exist!

**Linux/Mac:**
```bash
./setup.sh
```

**Windows:**
```bash
setup.bat
```

**Or manually:**
```bash
export DB_NAME=ecommerce_db
export POSTGRES_USER=spycon
export POSTGRES_PASSWORD=postgressPassword12345
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
```

### What the Setup Does:
1. ✅ Connects to PostgreSQL server
2. ✅ Creates `ecommerce_db` database if it doesn't exist
3. ✅ Creates all tables (users, products, categories, orders, etc.)
4. ✅ Inserts demo data with hashed passwords
5. ✅ Verifies the setup

### Manual Setup (Alternative)

1.  **Database Setup**:
    - Ensure PostgreSQL is running on `localhost:5432`
    - Default credentials: `spycon` / `postgressPassword12345`
    - Run the setup utility:
        ```bash
        mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.DatabaseSetup"
        ```

2.  **Run the Application**:
    ```bash
    mvn javafx:run
    ```

3.  **Run Tests**:
    ```bash
    mvn test
    ```

4.  **Run Performance Test**:
    ```bash
    mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.utils.PerformanceTestRunner"
    ```

## Performance Report
See [docs/performance-report.md](../SmartEcommerceSystem/doc/performance-report.md) for detailed metrics on Indexing and Caching improvements.

## Documentation
- [Database Design](../SmartEcommerceSystem/doc/database-design.md)
- [Performance Report](../SmartEcommerceSystem/doc/performance-report.md)

## Architecture
- **Presentation**: JavaFX (FXML + Controllers)
- **Service**: Business Logic, Caching, Sorting
- **DAO**: JDBC Data Access
- **Database**: MySQL (3NF Schema)
