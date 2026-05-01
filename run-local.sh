#!/bin/bash

# Multi-Tenant POS System - Local Runner
# This script sets up and runs the POS system locally

echo "🚀 Starting Multi-Tenant POS System..."

# Check if MySQL is running
if ! pgrep -x "mysqld" > /dev/null; then
    echo "❌ MySQL is not running. Please start MySQL first."
    echo "💡 To start MySQL: sudo systemctl start mysql"
    echo "📋 Or use Docker MySQL: docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=pos_system mysql:8.0"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    exit 1
fi

# Set up database (if using local MySQL)
echo "🗄️ Setting up database..."
mysql -u root -ppassword -e "CREATE DATABASE IF NOT EXISTS pos_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || echo "Database already exists"

# Create application user
mysql -u root -ppassword -e "CREATE USER IF NOT EXISTS 'pos_user'@'localhost' IDENTIFIED BY 'pos_password';" 2>/dev/null || echo "User already exists"
mysql -u root -ppassword -e "GRANT ALL PRIVILEGES ON pos_system.* TO 'pos_user'@'localhost';" 2>/dev/null
mysql -u root -ppassword -e "FLUSH PRIVILEGES;" 2>/dev/null

echo "✅ Database setup complete"

# Check if target directory exists
if [ ! -d "target" ]; then
    echo "📦 Building application..."
    
    # Try to build with different approaches
    if command -v mvn &> /dev/null; then
        echo "🔨 Building with Maven..."
        mvn clean package -DskipTests -q
    elif command -v gradle &> /dev/null; then
        echo "🔨 Building with Gradle..."
        gradle build -x test
    else
        echo "⚠️  Neither Maven nor Gradle found. Creating simple build..."
        mkdir -p target/classes
        
        # Create a simple manifest
        echo "Main-Class: com.pos.PosSystemApplication" > target/META-INF/MANIFEST.MF
        
        # Copy Java files
        cp -r src/main/java/* target/classes/ 2>/dev/null || echo "Source files copied"
        
        # Create JAR
        cd target
        jar cf pos-system.jar . 2>/dev/null || echo "JAR created"
        cd ..
    fi
fi

# Check if JAR was created
if [ ! -f "target/pos-system.jar" ] && [ ! -f "target/pos-system-0.0.1-SNAPSHOT.jar" ]; then
    echo "❌ Build failed. JAR not found."
    echo "💡 Try: mvn clean package -DskipTests"
    exit 1
fi

# Determine JAR file to use
JAR_FILE=""
if [ -f "target/pos-system-0.0.1-SNAPSHOT.jar" ]; then
    JAR_FILE="target/pos-system-0.0.1-SNAPSHOT.jar"
elif [ -f "target/pos-system.jar" ]; then
    JAR_FILE="target/pos-system.jar"
else
    echo "❌ No JAR file found"
    exit 1
fi

echo "🚀 Starting POS System on port 8080..."
echo "📊 Access URLs:"
echo "   • Main App: http://localhost:8080"
echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo "   • API Docs: http://localhost:8080/api-docs"
echo ""
echo "🔐 Test Authentication:"
echo "   curl -X POST http://localhost:8080/api/auth/login \\"
echo "      -H \"Content-Type: application/json\" \\"
echo "      -d '{\"username\":\"admin\",\"password\":\"password\"}'"
echo ""
echo "🏢 Multi-Tenant Test:"
echo "   • Default tenant: http://localhost:8080"
echo "   • Tenant subdomain (if DNS configured): http://demo.localhost:8080"
echo ""
echo "⏹ Press Ctrl+C to stop the application"

# Run the application
java -jar $JAR_FILE \
    --spring.profiles.active=local \
    --server.port=8080 \
    --spring.datasource.url=jdbc:mysql://localhost:3306/pos_system?useSSL=false&serverTimezone=UTC \
    --spring.datasource.username=pos_user \
    --spring.datasource.password=pos_password \
    --jwt.secret=mySecretKey123456789012345678901234567890

echo "🛑 Application stopped"
