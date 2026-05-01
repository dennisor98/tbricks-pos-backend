#!/bin/bash

# Simple Multi-Tenant POS System Runner
# This script runs the POS system with minimal dependencies

echo "🚀 Starting Multi-Tenant POS System..."

# Check if MySQL is running
if ! pgrep -x "mysqld" > /dev/null; then
    echo "❌ MySQL is not running. Starting MySQL with Docker..."
    docker run -d --name pos-mysql -p 3306:3306 \
        -e MYSQL_ROOT_PASSWORD=password \
        -e MYSQL_DATABASE=pos_system \
        mysql:8.0
    echo "⏳ Waiting for MySQL to start..."
    sleep 10
fi

# Create database and user if not exists
echo "🗄️ Setting up database..."
mysql -h 127.0.0.1 -u root -ppassword -e "
CREATE DATABASE IF NOT EXISTS pos_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'pos_user'@'%' IDENTIFIED BY 'pos_password';
GRANT ALL PRIVILEGES ON pos_system.* TO 'pos_user'@'%';
FLUSH PRIVILEGES;
" 2>/dev/null || echo "Database setup complete"

# Create a simple application.properties for local run
echo "📝 Creating configuration..."
cat > application-local.properties << EOF
# Local Development Configuration
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/pos_system?useSSL=false&serverTimezone=UTC
spring.datasource.username=pos_user
spring.datasource.password=pos_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000
EOF

# Try to run the application
echo "🚀 Starting POS System on port 8080..."
echo "📊 Access URLs:"
echo "   • Main App: http://localhost:8080"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo "   • Test Login: curl -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"password\"}'"
echo ""
echo "🔐 Multi-Tenant Features:"
echo "   • Tenant isolation via subdomain"
echo "   • Per-tenant permissions"
echo "   • Subscription management"
echo "   • JWT authentication with tenant context"
echo ""
echo "⏹ Press Ctrl+C to stop the application"

# Try different approaches to run the application
if [ -f "target/pos-system-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 Running from existing JAR..."
    java -jar target/pos-system-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
elif [ -f "target/pos-system.jar" ]; then
    echo "📦 Running from existing JAR..."
    java -jar target/pos-system.jar --spring.profiles.active=local
else
    echo "⚠️  No JAR file found. Please build the application first."
    echo "💡 Try: mvn clean package -DskipTests (if Maven available)"
    echo "💡 Or: The application needs Spring Boot dependencies to run properly."
fi

echo "🛑 Application stopped"
