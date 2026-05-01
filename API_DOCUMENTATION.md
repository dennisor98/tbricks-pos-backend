# POS System API Documentation

## Overview
This is a comprehensive multi-tenant Point of Sale (POS) system API with inventory management, analytics, and SaaS features. The system supports multiple tenants with complete data isolation, per-tenant permissions, and subscription management.

## Base URLs
- **Development**: `http://localhost:8080`
- **Production**: `https://api.pos-system.com`
- **Tenant-specific**: `https://{tenant}.api.pos-system.com`

## Authentication
The API uses JWT (JSON Web Token) authentication. Include the token in the Authorization header:

```
Authorization: Bearer {jwt_token}
```

### Multi-Tenant Authentication
- Tenant is automatically detected from the subdomain in the request URL
- JWT token includes tenant ID for subsequent requests
- Disabled tenants cannot access any API endpoints

## API Endpoints

### 1. Authentication (`/api/auth`)

#### Login
```http
POST /api/auth/login
```
Authenticate user with username and password. The tenant is automatically detected from the subdomain.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@demo.com",
    "role": "ADMIN",
    "tenantId": 1,
    "tenantName": "Demo Store"
  }
}
```

#### Register
```http
POST /api/auth/register
```
Register a new user in the system. The user will be associated with the current tenant.

**Request Body:**
```json
{
  "username": "newuser",
  "password": "password123",
  "email": "newuser@demo.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "5551234567",
  "role": "CASHIER"
}
```

### 2. Products (`/api/products`)

#### Get All Products
```http
GET /api/products
```
Retrieve all active products for the current tenant.

**Response:**
```json
[
  {
    "id": 1,
    "name": "Laptop Pro 15\"",
    "description": "High-performance laptop with 15-inch display",
    "sku": "LAP001",
    "price": 999.99,
    "quantity": 25,
    "minQuantity": 5,
    "maxQuantity": 100,
    "costPrice": 750.00,
    "imageUrl": "https://example.com/laptop.jpg",
    "active": true,
    "categoryId": 1,
    "categoryName": "Electronics"
  }
]
```

#### Create Product
```http
POST /api/products
```
Create a new product for the current tenant.

**Request Body:**
```json
{
  "name": "Wireless Keyboard",
  "description": "Ergonomic wireless keyboard",
  "sku": "KEY001",
  "price": 49.99,
  "quantity": 30,
  "minQuantity": 5,
  "maxQuantity": 200,
  "costPrice": 25.00,
  "imageUrl": "https://example.com/keyboard.jpg",
  "categoryId": 1
}
```

#### Update Stock
```http
PUT /api/products/{id}/stock?quantity=50
```
Update product stock quantity.

### 3. Categories (`/api/categories`)

#### Get All Categories
```http
GET /api/categories
```
Retrieve all active categories for the current tenant.

#### Create Category
```http
POST /api/categories
```
Create a new category for the current tenant.

**Request Body:**
```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "imageUrl": "https://example.com/electronics.jpg"
}
```

### 4. Customers (`/api/customers`)

#### Get All Customers
```http
GET /api/customers
```
Retrieve all active customers for the current tenant.

#### Create Customer
```http
POST /api/customers
```
Create a new customer for the current tenant.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john.doe@email.com",
  "phoneNumber": "5551234567",
  "address": "123 Main St, City, State"
}
```

### 5. Sales (`/api/sales`)

#### Get All Sales
```http
GET /api/sales
```
Retrieve all sales for the current tenant.

#### Create Sale
```http
POST /api/sales
```
Create a new sale transaction.

**Request Body:**
```json
{
  "customerId": 1,
  "paymentMethod": "CASH",
  "notes": "Regular customer",
  "saleItems": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 999.99,
      "discountAmount": 0.00
    }
  ]
}
```

#### Refund Sale
```http
POST /api/sales/{id}/refund
```
Process a refund for a sale.

### 6. Analytics (`/api/analytics`)

#### Get Dashboard
```http
GET /api/analytics/dashboard
```
Get dashboard data with sales summary, low stock alerts, and recent sales.

#### Get Sales Report
```http
GET /api/analytics/sales-report?startDate=2024-01-01&endDate=2024-01-31
```
Generate sales report for a specific date range.

### 7. Tenant Management (`/api/tenants`)

#### Get All Tenants
```http
GET /api/tenants
```
Retrieve all tenants in the system (System Admin only).

#### Create Tenant
```http
POST /api/tenants
```
Create a new tenant with subscription plan (System Admin only).

**Request Body:**
```json
{
  "name": "New Store",
  "subdomain": "newstore",
  "description": "New retail store",
  "companyName": "New Store Inc.",
  "contactEmail": "admin@newstore.com",
  "contactPhone": "5559876543",
  "maxUsers": 25,
  "subscriptionPlan": "BASIC",
  "subscriptionExpires": "2024-12-31T23:59:59"
}
```

#### Disable Tenant
```http
PUT /api/tenants/{id}/disable
```
Disable a tenant (System Admin only). Once disabled, all API requests for this tenant will be rejected.

#### Enable Tenant
```http
PUT /api/tenants/{id}/enable
```
Enable a tenant (System Admin only).

#### Check Tenant Status
```http
GET /api/tenants/subdomain/{subdomain}/active-status
```
Check if a tenant is active by subdomain.

### 8. Tenant Permissions (`/api/tenants/{tenantId}/permissions`)

#### Get Tenant Permissions
```http
GET /api/tenants/{tenantId}/permissions
```
Retrieve all permissions for a specific tenant.

#### Create Permission
```http
POST /api/tenants/{tenantId}/permissions
```
Create a new permission for a tenant.

**Request Body:**
```json
{
  "permissionName": "VIEW_PRODUCTS",
  "description": "View products",
  "enabled": true
}
```

## Error Responses

### Standard Error Format
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/products/999"
}
```

### Common Error Codes
- **401 Unauthorized**: Invalid or missing authentication token
- **403 Forbidden**: Insufficient permissions or tenant disabled
- **404 Not Found**: Resource not found
- **400 Bad Request**: Validation failed or business rule violation
- **500 Internal Server Error**: Unexpected server error

### Tenant-Specific Errors
- **403 Forbidden**: Tenant is disabled or subscription expired
- **400 Bad Request**: Maximum user limit reached for tenant
- **403 Forbidden**: Access denied - insufficient tenant permissions

## Multi-Tenant Features

### Tenant Isolation
- All data is isolated by tenant
- Users can only access data from their own tenant
- SKU, usernames, and emails must be unique within each tenant

### Subdomain Access
- Access tenant-specific data using subdomain: `https://{tenant}.api.pos-system.com`
- Tenant is automatically detected from request URL
- Disabled tenants return 403 Forbidden for all requests

### Permission System
- 18 granular permissions per tenant
- Permissions can be enabled/disabled per tenant
- Method-level security with custom permission validators

### Subscription Management
- Three subscription plans: BASIC, PROFESSIONAL, ENTERPRISE
- User limits enforced per subscription
- Auto-disable when subscription expires

## Rate Limiting
- API requests are rate limited per tenant
- Limits vary by subscription plan
- Exceeded limits return 429 Too Many Requests

## Data Models

### User Roles
- **ADMIN**: Full access to all features
- **MANAGER**: Can manage products, categories, customers, and sales
- **CASHIER**: Can create sales and view products

### Subscription Plans
- **BASIC**: Up to 25 users, basic features
- **PROFESSIONAL**: Up to 50 users, advanced features
- **ENTERPRISE**: Up to 100 users, all features + priority support

### Payment Methods
- CASH
- CARD
- MOBILE_MONEY
- BANK_TRANSFER
- CREDIT

### Payment Status
- PENDING
- PAID
- REFUNDED
- PARTIALLY_REFUNDED

## Testing

### Sample Credentials
- **Username**: admin
- **Password**: password
- **Tenant**: demo (access via `http://demo.localhost:8080`)

### Test Endpoints
1. Login to get JWT token
2. Use token in Authorization header for subsequent requests
3. Test tenant isolation by accessing different subdomains

## Support
For API support and documentation updates, contact:
- Email: support@pos-system.com
- Documentation: https://docs.pos-system.com
- Status Page: https://status.pos-system.com
