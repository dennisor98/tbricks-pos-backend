# Quick Start Guide - Multi-Tenant POS System

## Option 1: Run Locally (Recommended for Development)

Since you're having Docker/network issues, let's run the application locally first:

### Prerequisites
- Java 17+ installed
- MySQL 8.0+ running locally
- Maven 3.6+

### Step 1: Setup Database
```sql
-- Create database
CREATE DATABASE pos_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'pos_user'@'%' IDENTIFIED BY 'pos_password';
GRANT ALL PRIVILEGES ON pos_system.* TO 'pos_user'@'%';
FLUSH PRIVILEGES;
```

### Step 2: Configure Application
Create `application.properties`:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/pos_system?useSSL=false&serverTimezone=UTC
spring.datasource.username=pos_user
spring.datasource.password=pos_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Server
server.port=8080

# JWT
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000
```

### Step 3: Run Application
```bash
# If Maven is available
mvn spring-boot:run

# Or use Java directly
java -jar target/pos-system-0.0.1-SNAPSHOT.jar
```

### Step 4: Access Application
- **Main App**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### Test Multi-Tenant Access
```bash
# Test login with default tenant
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Test with tenant subdomain (if DNS configured)
curl -X POST http://demo.localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

## Option 2: Docker Setup (When Network is Fixed)

### Quick Docker Commands
```bash
# Build application locally first
mvn clean package -DskipTests

# Run with single Docker container
docker run -d \
  --name pos-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/pos_system \
  -e SPRING_DATASOURCE_USERNAME=pos_user \
  -e SPRING_DATASOURCE_PASSWORD=pos_password \
  -v $(pwd)/target/pos-system-0.0.1-SNAPSHOT.jar:/app/app.jar \
  eclipse-temurin:17-jre-alpine \
  java -jar /app/app.jar
```

### Docker Compose (Simplified)
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: pos-mysql
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: pos_system
      MYSQL_USER: pos_user
      MYSQL_PASSWORD: pos_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  pos-app:
    build: .
    container_name: pos-app
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/pos_system?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: pos_user
      SPRING_DATASOURCE_PASSWORD: pos_password
    depends_on:
      - mysql
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  mysql_data:
```

## Option 3: Fix Network Issues

### Update Package Manager
```bash
# Try different package manager mirrors
sudo apt update

# Install Docker Compose
sudo apt install docker-compose-plugin

# Or use pip
pip install docker-compose
```

### Use Different Docker Registry
```bash
# Configure Docker to use different registry
sudo mkdir -p /etc/docker
echo '{"registry-mirrors": ["https://registry-1.docker.io"]}' | sudo tee /etc/docker/daemon.json
sudo systemctl restart docker
```

## Testing the Application

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```

### 2. Authentication Test
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

### 3. API Test
```bash
# Get products (will need JWT token from login)
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Multi-Tenant Features

### Tenant Isolation Test
1. Login as admin
2. Create products (they belong to tenant 1)
3. Different tenants can't see each other's data
4. Tenant disable blocks all access

### Permission System Test
1. Check tenant permissions: `/api/tenants/1/permissions`
2. Enable/disable specific permissions
3. Test API access based on permissions

## Troubleshooting

### Common Issues
1. **Port 8080 in use**: Change server.port in application.properties
2. **Database connection**: Check MySQL is running and credentials
3. **Build failures**: Ensure Java 17+ and Maven 3.6+
4. **JWT errors**: Check jwt.secret configuration

### Logs
```bash
# Application logs
tail -f logs/spring.log

# Docker logs
docker logs pos-app
```

## Production Deployment

### Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=production
export JWT_SECRET=your-strong-secret-key
export MYSQL_ROOT_PASSWORD=your-strong-password
```

### Security
- Change default passwords
- Enable SSL in production
- Configure firewall rules
- Set up monitoring

## Support

For immediate help:
1. Check application logs
2. Verify database connection
3. Test health endpoints
4. Review configuration files

The multi-tenant POS system is ready to run with full SaaS features including:
- Tenant isolation
- Per-tenant permissions
- Subscription management
- Load balancing capability
- Comprehensive API documentation
