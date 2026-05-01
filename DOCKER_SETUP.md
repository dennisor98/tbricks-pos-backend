# Docker Setup Guide

## Overview
This Docker setup creates a complete multi-tenant POS system with MySQL database and 8 Spring Boot application instances behind an Nginx load balancer.

## Architecture
```
Nginx Load Balancer (Port 8080)
    |
    +-- Spring Boot App 1 (Port 8090)
    +-- Spring Boot App 2 (Port 8091)
    +-- Spring Boot App 3 (Port 8092)
    +-- Spring Boot App 4 (Port 8093)
    +-- Spring Boot App 5 (Port 8094)
    +-- Spring Boot App 6 (Port 8095)
    +-- Spring Boot App 7 (Port 8096)
    +-- Spring Boot App 8 (Port 8097)
    |
    +-- MySQL Database (Port 3306)
```

## Prerequisites
- Docker Desktop installed
- Docker Compose installed
- At least 4GB RAM available
- Ports 8080-8097 and 3306 available

## Quick Start

### 1. Build and Start All Services
```bash
# Build and start all services in the background
docker-compose up -d --build

# View logs
docker-compose logs -f

# Check service status
docker-compose ps
```

### 2. Access the Application
- **Main Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Documentation**: http://localhost:8080/api-docs
- **MySQL**: localhost:3306 (user: pos_user, password: pos_password)

### 3. Test the Setup
```bash
# Test health check
curl http://localhost:8080/health

# Test API through load balancer
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Test individual instances
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

## Service Details

### MySQL Database
- **Container**: pos-mysql
- **Image**: mysql:8.0
- **Port**: 3306
- **Database**: pos_system
- **Credentials**: pos_user / pos_password
- **Data Persistence**: Docker volume `mysql_data`

### Spring Boot Applications
- **8 Instances**: pos-app-1 through pos-app-8
- **Internal Port**: 8080
- **External Ports**: 8090-8097
- **Health Checks**: Enabled with 30s intervals
- **Log Persistence**: Docker volume `app_logs`
- **Instance IDs**: 1-8 for identification

### Nginx Load Balancer
- **Container**: pos-nginx
- **Image**: nginx:alpine
- **Port**: 8080 (external)
- **Algorithm**: Least Connections
- **Health Checks**: Built-in failover
- **Timeout**: 30s for API endpoints

## Configuration

### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/pos_system
SPRING_DATASOURCE_USERNAME=pos_user
SPRING_DATASOURCE_PASSWORD=pos_password

# Server Configuration
SERVER_PORT=8080
APP_INSTANCE_ID=1

# JWT Configuration
JWT_SECRET=mySecretKey123456789012345678901234567890
JWT_EXPIRATION=86400000
```

### Docker Compose Profiles
```bash
# Development profile (default)
docker-compose up -d

# Production profile
docker-compose --profile production up -d

# Testing profile
docker-compose --profile testing up -d
```

## Scaling and Load Balancing

### Horizontal Scaling
```bash
# Scale specific services
docker-compose up -d --scale pos-app-1=3

# Add more instances
docker-compose up -d --scale pos-app-2=2
```

### Load Balancer Configuration
The Nginx load balancer uses:
- **Algorithm**: Least Connections
- **Health Checks**: Automatic failover
- **Timeout**: 30s for API endpoints
- **Retry Logic**: 3 retries with 30s timeout

## Monitoring and Health Checks

### Health Check Endpoints
```bash
# Nginx health check
curl http://localhost:8080/health

# Spring Boot health checks
curl http://localhost:8090/actuator/health
curl http://localhost:8091/actuator/health
# ... for all instances
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f pos-app-1
docker-compose logs -f mysql
docker-compose logs -f nginx

# Application logs (mounted volume)
docker exec pos-app-1 ls /app/logs
```

### Resource Monitoring
```bash
# Container resource usage
docker stats

# Detailed container info
docker inspect pos-app-1

# Network information
docker network ls
docker network inspect pos-pos-network
```

## Data Persistence

### MySQL Data
- **Volume**: `mysql_data`
- **Location**: Docker-managed volume
- **Backup**: `docker exec mysql mysqldump -u root -p pos_system > backup.sql`

### Application Logs
- **Volume**: `app_logs`
- **Location**: `/app/logs` in containers
- **Rotation**: Configure in application-docker.properties

## Troubleshooting

### Common Issues

#### 1. Port Conflicts
```bash
# Check what's using ports
netstat -tulpn | grep :8080
netstat -tulpn | grep :3306

# Kill conflicting processes
sudo kill -9 <PID>
```

#### 2. Database Connection Issues
```bash
# Check MySQL container
docker-compose logs mysql

# Test database connection
docker exec -it mysql mysql -u pos_user -p pos_system

# Restart database
docker-compose restart mysql
```

#### 3. Application Startup Issues
```bash
# Check application logs
docker-compose logs pos-app-1

# Debug container
docker exec -it pos-app-1 /bin/bash

# Restart specific instance
docker-compose restart pos-app-1
```

#### 4. Load Balancer Issues
```bash
# Check Nginx configuration
docker exec pos-nginx nginx -t

# Reload Nginx
docker exec pos-nginx nginx -s reload

# Check upstream status
docker exec pos-nginx curl http://pos-app-1:8080/health
```

### Performance Tuning

#### MySQL Optimization
```sql
-- Add to my.cnf for better performance
[mysqld]
innodb_buffer_pool_size = 256M
innodb_log_file_size = 64M
max_connections = 200
query_cache_size = 32M
```

#### Spring Boot Tuning
```properties
# Add to application-docker.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
server.tomcat.threads.max=200
server.tomcat.max-connections=10000
```

## Security Considerations

### Network Security
- All services run in isolated Docker network
- Only necessary ports exposed to host
- Database not accessible from outside Docker network

### Data Security
- Use strong passwords in production
- Enable SSL for database connections
- Regular database backups
- Monitor access logs

### Application Security
- JWT tokens with proper expiration
- Rate limiting per tenant
- Input validation and sanitization
- Regular security updates

## Production Deployment

### Environment Setup
```bash
# Production environment variables
export SPRING_PROFILES_ACTIVE=production
export MYSQL_ROOT_PASSWORD=strong_password
export JWT_SECRET=very_strong_secret_key

# Deploy with production profile
docker-compose --profile production up -d
```

### SSL Configuration
```bash
# Add SSL certificates to Nginx
mkdir -p ./ssl
# Copy certificates to ./ssl/

# Update nginx.conf for SSL
# Add SSL server block configuration
```

### Backup Strategy
```bash
# Automated backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec mysql mysqldump -u root -p pos_system > backup_${DATE}.sql
aws s3 cp backup_${DATE}.sql s3://backups/mysql/
```

## Development Workflow

### Local Development
```bash
# Start only database
docker-compose up -d mysql

# Run application locally
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Test with Docker instances
docker-compose up -d pos-app-1
```

### Testing
```bash
# Run integration tests
docker-compose --profile testing up -d

# Load testing
ab -n 1000 -c 10 http://localhost:8080/api/products

# Performance monitoring
docker stats --no-stream
```

## Support

For issues with the Docker setup:
1. Check logs: `docker-compose logs`
2. Verify configuration: `docker-compose config`
3. Test connectivity: `docker network inspect pos-pos-network`
4. Review resource usage: `docker stats`

## Cleanup

### Stop and Remove All Services
```bash
# Stop all services
docker-compose down

# Remove volumes (WARNING: This deletes all data)
docker-compose down -v

# Remove images
docker-compose down --rmi all
```

### Clean Up Resources
```bash
# Remove unused containers
docker container prune

# Remove unused images
docker image prune

# Remove unused volumes
docker volume prune
```
