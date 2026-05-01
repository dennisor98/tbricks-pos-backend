# POS System - Modern Point of Sale with Inventory & Analytics

A comprehensive, modern Point of Sale (POS) system built with Spring Boot, featuring inventory management, sales tracking, customer management, and advanced analytics. The system uses JWT authentication, Spring Security, MySQL database with JPA, and Lombok for clean code.

## Features

### Core Functionality
- **User Management**: Role-based access control (Admin, Manager, Cashier)
- **Product Management**: Full CRUD operations with SKU tracking
- **Category Management**: Organize products by categories
- **Customer Management**: Customer profiles with loyalty points
- **Sales Processing**: Complete transaction management with multiple payment methods
- **Inventory Management**: Real-time stock tracking and low-stock alerts

### Analytics & Reporting
- **Dashboard**: Real-time sales overview and key metrics
- **Sales Reports**: Detailed sales analytics by date range
- **Inventory Reports**: Stock levels and valuation
- **Top Selling Products**: Performance analysis
- **Revenue Tracking**: Daily, weekly, and monthly summaries

### Security & Authentication
- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access**: Granular permissions by user role
- **Spring Security**: Enterprise-grade security framework

## Technology Stack

- **Backend**: Spring Boot 3.2.5
- **Security**: Spring Security with JWT
- **Database**: MySQL with JPA/Hibernate
- **Code Quality**: Lombok for boilerplate reduction
- **Documentation**: OpenAPI 3.0 with Swagger UI
- **Validation**: Jakarta Bean Validation
- **Build Tool**: Maven

## API Documentation

The system provides comprehensive REST APIs organized by module:

### Authentication APIs
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Product Management APIs
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get product by SKU
- `GET /api/products/category/{categoryId}` - Get products by category
- `GET /api/products/search?term={term}` - Search products
- `GET /api/products/low-stock` - Get low stock products
- `POST /api/products` - Create product (Admin/Manager)
- `PUT /api/products/{id}` - Update product (Admin/Manager)
- `DELETE /api/products/{id}` - Delete product (Admin/Manager)
- `PUT /api/products/{id}/stock` - Update stock (Admin/Manager)

### Category Management APIs
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category (Admin/Manager)
- `PUT /api/categories/{id}` - Update category (Admin/Manager)
- `DELETE /api/categories/{id}` - Delete category (Admin/Manager)

### Customer Management APIs
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/search?term={term}` - Search customers
- `POST /api/customers` - Create customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer (Admin/Manager)
- `PUT /api/customers/{id}/loyalty-points` - Update loyalty points (Admin/Manager)

### Sales Management APIs
- `GET /api/sales` - Get all sales
- `GET /api/sales/{id}` - Get sale by ID
- `GET /api/sales/invoice/{invoiceNumber}` - Get sale by invoice number
- `GET /api/sales/date-range` - Get sales by date range
- `POST /api/sales` - Create sale
- `POST /api/sales/{saleId}/refund` - Refund sale items (Admin/Manager)

### Analytics APIs
- `GET /api/analytics/dashboard` - Get dashboard data (Admin/Manager)
- `GET /api/analytics/sales-report` - Get sales report (Admin/Manager)
- `GET /api/analytics/top-selling-products` - Get top selling products (Admin/Manager)
- `GET /api/analytics/inventory-report` - Get inventory report (Admin/Manager)

## Database Schema

### Core Tables
- **users**: User accounts with roles and authentication
- **categories**: Product categories
- **products**: Product inventory with pricing and stock
- **customers**: Customer information and loyalty points
- **sales**: Sales transactions and payment details
- **sale_items**: Individual line items for each sale

## User Roles & Permissions

### Admin
- Full system access
- User management
- All CRUD operations
- Complete analytics access

### Manager
- Product and category management
- Sales operations and refunds
- Analytics and reporting
- Customer management

### Cashier
- Product viewing (read-only)
- Sales processing
- Customer management (basic)
- Limited analytics

## Installation & Setup

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Database Setup
1. Create MySQL database:
   ```sql
   CREATE DATABASE pos_system;
   ```

2. Update database credentials in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/pos_system
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

### Running the Application
1. Clone the repository
2. Navigate to project directory
3. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

### API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Configuration

### JWT Configuration
Update JWT settings in `application.properties`:
```properties
jwt.secret=your-secret-key
jwt.expiration=86400000  # 24 hours in milliseconds
```

### Server Configuration
```properties
server.port=8080
```

## Usage Examples

### Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Product",
    "sku": "SKU001",
    "price": 19.99,
    "quantity": 100,
    "minQuantity": 10,
    "categoryId": 1
  }'
```

### Create Sale
```bash
curl -X POST http://localhost:8080/api/sales \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "subtotal": 19.99,
    "taxAmount": 1.60,
    "discountAmount": 0.00,
    "totalAmount": 21.59,
    "paymentMethod": "CASH",
    "paymentStatus": "PAID",
    "saleItems": [{
      "productId": 1,
      "quantity": 1,
      "unitPrice": 19.99,
      "totalPrice": 19.99,
      "discountAmount": 0.00
    }]
  }'
```

## Development

### Project Structure
```
src/main/java/com/pos/
  controller/     # REST API controllers
  dto/           # Data Transfer Objects
  entity/        # JPA entities
  exception/     # Exception handling
  repository/    # JPA repositories
  security/      # Security configuration
  service/       # Business logic
```

### Adding New Features
1. Create entity in `entity/` package
2. Create repository interface
3. Implement service layer
4. Create DTOs for request/response
5. Add REST controller
6. Update security configuration if needed

## Testing

### Running Tests
```bash
mvn test
```

### Test Coverage
The system includes comprehensive test coverage for:
- Service layer business logic
- Repository layer data access
- Controller layer API endpoints
- Security configuration

## Production Deployment

### Environment Variables
- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `JWT_SECRET`: JWT signing secret

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/pos-system-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Security Considerations

- JWT tokens should be stored securely on client side
- Database credentials should use environment variables
- HTTPS should be enabled in production
- Regular security updates for dependencies
- Input validation on all API endpoints

## Contributing

1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Submit pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions, please create an issue in the repository or contact the development team.
