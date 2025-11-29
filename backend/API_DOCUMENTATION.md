# DeliverXY API Documentation

## Overview
DeliverXY is a crowdsourced delivery platform that connects clients with delivery agents. This API provides endpoints for user management, delivery operations, payments, and real-time communication.

## Base URL
```
http://localhost:8080/api
```

## Authentication
The API uses JWT (JSON Web Tokens) for authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## API Endpoints

### 1. Authentication (`/api/auth`)

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "CLIENT"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Authorization: Bearer <refresh_token>
```

#### Logout
```http
POST /api/auth/logout
Authorization: Bearer <access_token>
```

### 2. User Management (`/api/user`)

#### Get All Users
```http
GET /api/user
Authorization: Bearer <token>
```

#### Get User by ID
```http
GET /api/user/{id}
Authorization: Bearer <token>
```

#### Create User
```http
POST /api/user/add-user
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "new_user",
  "email": "new@example.com",
  "password": "password123",
  "role": "CLIENT"
}
```

#### Update User
```http
POST /api/user/edit-user/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Updated Name",
  "email": "updated@example.com"
}
```

#### Delete User
```http
DELETE /api/user/delete-user/{id}
Authorization: Bearer <token>
```

### 3. Delivery Management (`/api/deliveries`)

#### Get All Deliveries
```http
GET /api/deliveries
Authorization: Bearer <token>
```

#### Get Delivery by ID
```http
GET /api/deliveries/{id}
Authorization: Bearer <token>
```

#### Get Deliveries by Status
```http
GET /api/deliveries/status/{status}
Authorization: Bearer <token>
```

#### Get Deliveries by Client
```http
GET /api/deliveries/client/{clientId}
Authorization: Bearer <token>
```

#### Get Deliveries by Agent
```http
GET /api/deliveries/agent/{agentId}
Authorization: Bearer <token>
```

#### Get Nearby Deliveries
```http
GET /api/deliveries/nearby?latitude=40.7128&longitude=-74.0060&radius=5.0
Authorization: Bearer <token>
```

#### Create Delivery
```http
POST /api/deliveries?clientId=1
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Package Delivery",
  "description": "Small package from downtown",
  "pickupAddress": "123 Main St, Downtown",
  "dropoffAddress": "456 Oak Ave, Uptown",
  "basePrice": 25.00,
  "packageType": "SMALL",
  "isUrgent": false,
  "isFragile": false
}
```

#### Update Delivery
```http
PUT /api/deliveries/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description"
}
```

#### Assign Delivery to Agent
```http
POST /api/deliveries/{id}/assign?agentId=2
Authorization: Bearer <token>
```

#### Update Delivery Status
```http
PUT /api/deliveries/{id}/status?status=PICKED_UP
Authorization: Bearer <token>
```

#### Update Delivery Location
```http
PUT /api/deliveries/{id}/location?latitude=40.7128&longitude=-74.0060
Authorization: Bearer <token>
```

#### Delete Delivery
```http
DELETE /api/deliveries/{id}
Authorization: Bearer <token>
```

### 4. KYC Management (`/api/kyc`)

#### Submit KYC Documents
```http
POST /api/kyc/submit
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
  "file": <file>,
  "documentType": "id_front",
  "userId": 1
}
```

#### Get Pending KYC
```http
GET /api/kyc/pending
Authorization: Bearer <token>
```

#### Approve KYC
```http
POST /api/kyc/{userId}/approve
Authorization: Bearer <token>
```

#### Reject KYC
```http
POST /api/kyc/{userId}/reject?reason=Invalid document
Authorization: Bearer <token>
```

#### Get KYC Status
```http
GET /api/kyc/{userId}/status
Authorization: Bearer <token>
```

### 5. Rating System (`/api/ratings`)

#### Create Rating
```http
POST /api/ratings
Authorization: Bearer <token>
Content-Type: application/json

{
  "delivery": {"id": 1},
  "reviewer": {"id": 1},
  "reviewed": {"id": 2},
  "rating": 5,
  "review": "Excellent service!",
  "ratingType": "CLIENT_TO_AGENT"
}
```

#### Get Ratings by Delivery
```http
GET /api/ratings/delivery/{deliveryId}
Authorization: Bearer <token>
```

#### Get Ratings by User
```http
GET /api/ratings/user/{userId}
Authorization: Bearer <token>
```

#### Get Average Rating
```http
GET /api/ratings/average/{userId}
Authorization: Bearer <token>
```

#### Update Rating
```http
PUT /api/ratings/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "rating": 4,
  "review": "Updated review"
}
```

#### Delete Rating
```http
DELETE /api/ratings/{id}
Authorization: Bearer <token>
```

### 6. Payment System (`/api/payments`)

#### Create Payment Intent
```http
POST /api/payments/create-intent?deliveryId=1&payerId=1
Authorization: Bearer <token>
```

#### Confirm Payment
```http
POST /api/payments/confirm?paymentIntentId=pi_1234567890
Authorization: Bearer <token>
```

#### Refund Payment
```http
POST /api/payments/refund?paymentId=1&amount=25.00&reason=Customer request
Authorization: Bearer <token>
```

#### Get User Payments
```http
GET /api/payments/user/{userId}
Authorization: Bearer <token>
```

#### Get Delivery Payments
```http
GET /api/payments/delivery/{deliveryId}
Authorization: Bearer <token>
```

#### Get Payment by ID
```http
GET /api/payments/{paymentId}
Authorization: Bearer <token>
```

#### Top Up Wallet
```http
POST /api/payments/wallet/topup?userId=1&amount=100.00
Authorization: Bearer <token>
```

#### Withdraw from Wallet
```http
POST /api/payments/wallet/withdraw?userId=1&amount=50.00
Authorization: Bearer <token>
```

#### Stripe Webhook
```http
POST /api/payments/stripe/webhook
Content-Type: application/json
Stripe-Signature: <signature>

{
  "type": "payment_intent.succeeded",
  "data": {...}
}
```

### 7. File Upload (`/api/upload`)

#### Upload KYC Document
```http
POST /api/upload/kyc
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
  "file": <file>,
  "documentType": "id_front",
  "userId": 1
}
```

#### Upload Profile Image
```http
POST /api/upload/profile
Authorization: Bearer <token>
Content-Type: multipart/form-data

{
  "file": <file>,
  "userId": 1
}
```

#### Validate File
```http
GET /api/upload/validate?file=<file>
Authorization: Bearer <token>
```

#### Delete File
```http
DELETE /api/upload/file?fileUrl=/uploads/kyc/1/id_front_123.jpg
Authorization: Bearer <token>
```

### 8. Admin Operations (`/api/admin`)

#### Get Dashboard Stats
```http
GET /api/admin/dashboard
Authorization: Bearer <token>
```

#### Get All Users
```http
GET /api/admin/users
Authorization: Bearer <token>
```

#### Block User
```http
POST /api/admin/users/{userId}/block
Authorization: Bearer <token>
```

#### Unblock User
```http
POST /api/admin/users/{userId}/unblock
Authorization: Bearer <token>
```

#### Get All Deliveries
```http
GET /api/admin/deliveries
Authorization: Bearer <token>
```

#### Assign Delivery to Agent
```http
POST /api/admin/deliveries/{deliveryId}/assign?agentId=2
Authorization: Bearer <token>
```

## Data Models

### User Roles
- `CLIENT` - Can request deliveries
- `AGENT` - Can accept and complete deliveries
- `ADMIN` - Can manage users and system

### Delivery Statuses
- `PENDING` - Created, waiting for agent
- `ASSIGNED` - Agent accepted
- `PICKED_UP` - Package collected
- `IN_TRANSIT` - On the way
- `OUT_FOR_DELIVERY` - Near destination
- `DELIVERED` - Successfully delivered
- `FAILED` - Delivery failed
- `CANCELLED` - Cancelled by user or system
- `EXPIRED` - Time expired

### Payment Statuses
- `PENDING` - Payment initiated
- `PROCESSING` - Payment being processed
- `COMPLETED` - Payment successful
- `FAILED` - Payment failed
- `CANCELLED` - Payment cancelled
- `REFUNDED` - Payment refunded
- `PARTIALLY_REFUNDED` - Partial refund

### -- First order discount
INSERT INTO promo_codes (code, description, discount_type, discount_value, is_first_order_only, is_active)
VALUES ('FIRST50', 'First order 50 MKD off', 'FIXED_AMOUNT', 50, true, true);

-- Percentage discount
INSERT INTO promo_codes (code, description, discount_type, discount_value, max_discount_amount, min_order_amount, is_active)
VALUES ('SKOPJE20', '20% off (max 100 MKD)', 'PERCENTAGE', 20, 100, 150, true);

-- Weekend special
INSERT INTO promo_codes (code, description, discount_type, discount_value, is_active)
VALUES ('WEEKEND30', 'Weekend special 30 MKD off', 'FIXED_AMOUNT', 30, true);

### KYC Statuses
- `PENDING` - No documents submitted
- `SUBMITTED` - Documents submitted, waiting review
- `VERIFIED` - Documents verified and approved
- `REJECTED` - Documents rejected
- `EXPIRED` - Documents expired

## Error Handling

The API returns appropriate HTTP status codes:

- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

Error responses include a message field:
```json
{
  "message": "Error description"
}
```

## Rate Limiting

API endpoints are subject to rate limiting to prevent abuse. Limits are:
- Authentication endpoints: 5 requests per minute
- General endpoints: 100 requests per minute
- File uploads: 10 requests per minute

## WebSocket Endpoints

For real-time features, connect to WebSocket endpoints:

```
ws://localhost:8080/ws
```

### Topics
- `/topic/delivery/{id}/location` - Delivery location updates
- `/topic/delivery/{id}/status` - Delivery status changes
- `/topic/delivery/{id}/agent-location` - Agent location updates
- `/topic/agents/new-delivery` - New delivery notifications
- `/topic/delivery/{id}/tracking` - Tracking start/stop events

## Testing

Use the provided sample data for testing:
1. Run `schema.sql` to create tables
2. Run `data.sql` to populate with sample data
3. Use test credentials:
   - Admin: `admin@deliverxy.com` / `password`
   - Client: `john@example.com` / `password`
   - Agent: `jane@example.com` / `password`

## Support

For API support and questions, contact the development team or refer to the project documentation. 