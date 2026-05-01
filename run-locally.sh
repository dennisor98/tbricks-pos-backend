#!/bin/bash

# Multi-Tenant POS System - Local Runner
# This script runs the application locally to access Swagger UI

echo "🚀 Starting Multi-Tenant POS System locally..."

# Start MySQL if not running
if ! pgrep -x "mysqld" > /dev/null; then
    echo "🗄️ Starting MySQL..."
    docker run -d --name pos-mysql-local -p 3306:3306 \
        -e MYSQL_ROOT_PASSWORD=password \
        -e MYSQL_DATABASE=pos_system \
        -e MYSQL_USER=pos_user \
        -e MYSQL_PASSWORD=pos_password \
        mysql:8.0
    echo "⏳ Waiting for MySQL to start..."
    sleep 15
fi

# Create database and user if not exists
echo "🗄️ Setting up database..."
mysql -h 127.0.0.1 -u root -ppassword -e "
CREATE DATABASE IF NOT EXISTS pos_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'pos_user'@'%' IDENTIFIED BY 'pos_password';
GRANT ALL PRIVILEGES ON pos_system.* TO 'pos_user'@'%';
FLUSH PRIVILEGES;
" 2>/dev/null || echo "Database setup complete"

echo "🚀 Starting POS System on port 8080..."
echo "📊 Access URLs:"
echo "   • Main App: http://localhost:8080"
echo "   • Swagger UI: http://localhost:8080/swagger-ui.html"
echo "   • API Docs: http://localhost:8080/api-docs"
echo "   • Health Check: http://localhost:8080/actuator/health"
echo ""
echo "🔐 Test Authentication:"
echo "   curl -X POST http://localhost:8080/api/auth/login \\"
echo "      -H \"Content-Type: application/json\" \\"
echo "      -d '{\"username\":\"admin\",\"password\":\"password\"}'"
echo ""
echo "⏹ Press Ctrl+C to stop the application"
echo ""

# Try to run with Spring Boot dependencies from local Maven repository
echo "🔨 Trying to build JAR using Spring Boot Maven plugin..."
# Try to use Spring Boot Maven plugin to build the JAR
if [ -f "pom.xml" ]; then
    echo "📦 Building application JAR..."
    # Try to find and use mvnw if available
    if [ -f "mvnw" ]; then
        chmod +x mvnw
        ./mvnw clean package -DskipTests -q
    else
        echo "⚠️  Maven wrapper not found"
        echo "💡 Please run: mvn clean package -DskipTests (if Maven is available)"
        echo "🔄 Or install Maven to build the application"
        exit 1
    fi
fi

# Check if JAR was created
if [ -f "target/pos-system-0.0.1-SNAPSHOT.jar" ]; then
    echo "✅ JAR built successfully"
    echo "🚀 Running application from JAR..."
    java -jar target/pos-system-0.0.1-SNAPSHOT.jar \
        --spring.profiles.active=local \
        --server.port=8080 \
        --spring.datasource.url=jdbc:mysql://localhost:3306/pos_system?useSSL=false&serverTimezone=UTC \
        --spring.datasource.username=pos_user \
        --spring.datasource.password=pos_password \
        --jwt.secret=mySecretKey123456789012345678901234567890
else
    echo "❌ JAR build failed"
    echo "💡 Alternative: Running from compiled classes (may have dependency issues)"
    echo "🔨 Building classpath from local Maven repository..."
    CLASSPATH="target/classes"
    CLASSPATH="$CLASSPATH:$(find ~/.m2/repository -name '*.jar' 2>/dev/null | grep -v sources | grep -v javadoc | tr '\n' ':')"
    CLASSPATH="${CLASSPATH%:}"
    java -cp "$CLASSPATH" com.pos.PosSystemApplication
fi

echo "🛑 Application stopped"
