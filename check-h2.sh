#!/bin/bash

echo "🔍 H2 Database Check Script"
echo "=========================="

# Check if application is running
echo "1️⃣ Checking application health..."
HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null)
if [ $? -eq 0 ]; then
    echo "✅ Application is running"
    echo "Health: $HEALTH"
else
    echo "❌ Application is not running"
    echo "Please start with: ./gradlew bootRun --args='--spring.profiles.active=h2'"
    exit 1
fi

echo ""
echo "2️⃣ Testing H2 Database via API..."

# Create a test member
echo "Creating test member..."
MEMBER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User",
    "phoneNumber": "010-1234-5678"
  }')

echo "Member created: $MEMBER_RESPONSE"
MEMBER_ID=$(echo $MEMBER_RESPONSE | jq -r '.id')

# Get the member back
echo "Retrieving member from H2..."
curl -s http://localhost:8080/api/members/$MEMBER_ID | jq .

# Create a test order
echo "Creating test order..."
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": '$MEMBER_ID',
    "orderItems": [
      {
        "productName": "Test Product",
        "productDescription": "A test product for H2 database testing",
        "quantity": 1,
        "unitPrice": 10000
      }
    ]
  }')

echo "Order created: $ORDER_RESPONSE"
ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')

# Get the order back
echo "Retrieving order from H2..."
curl -s http://localhost:8080/api/orders/$ORDER_ID | jq .

# Create a test payment
echo "Creating test payment..."
PAYMENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": '$ORDER_ID',
    "paymentMethod": "CREDIT_CARD",
    "amount": 10000
  }')

echo "Payment created: $PAYMENT_RESPONSE"
PAYMENT_ID=$(echo $PAYMENT_RESPONSE | jq -r '.id')

# Get the payment back
echo "Retrieving payment from H2..."
curl -s http://localhost:8080/api/payments/$PAYMENT_ID | jq .

echo ""
echo "3️⃣ H2 Console Access:"
echo "🌐 Open: http://localhost:8080/h2-console"
echo "📊 JDBC URL: jdbc:h2:mem:devdb"
echo "👤 Username: sa"
echo "🔑 Password: (empty)"
echo ""
echo "4️⃣ Sample SQL Queries:"
echo "SELECT * FROM members;"
echo "SELECT * FROM orders;"
echo "SELECT * FROM order_items;"
echo "SELECT * FROM payments;"
echo ""
echo "✅ H2 Database check completed!"
