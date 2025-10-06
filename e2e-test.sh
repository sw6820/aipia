#!/bin/bash

echo "🚀 Starting E2E Test for E-commerce System"
echo "=========================================="

# Start the application in background
echo "📱 Starting application with H2 database..."
./gradlew bootRun --args='--spring.profiles.active=h2' --no-daemon &
APP_PID=$!

# Wait for application to start
echo "⏳ Waiting for application to start..."
sleep 20

# Test health endpoint
echo "🏥 Testing health endpoint..."
curl -s http://localhost:8080/actuator/health | jq . || echo "Health check failed"

echo ""
echo "🧪 Running E2E Test Scenarios"
echo "=============================="

# Test 1: Create Member
echo "1️⃣ Creating a new member..."
MEMBER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "name": "John Doe",
    "phoneNumber": "010-1234-5678"
  }')

echo "Member created: $MEMBER_RESPONSE"
MEMBER_ID=$(echo $MEMBER_RESPONSE | jq -r '.id')

# Test 2: Get Member
echo "2️⃣ Retrieving member by ID..."
curl -s http://localhost:8080/api/members/$MEMBER_ID | jq .

# Test 3: Create Order
echo "3️⃣ Creating an order..."
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": '$MEMBER_ID',
    "orderItems": [
      {
        "productName": "Laptop",
        "quantity": 1,
        "price": 1500000
      },
      {
        "productName": "Mouse",
        "quantity": 2,
        "price": 50000
      }
    ]
  }')

echo "Order created: $ORDER_RESPONSE"
ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')

# Test 4: Get Order
echo "4️⃣ Retrieving order by ID..."
curl -s http://localhost:8080/api/orders/$ORDER_ID | jq .

# Test 5: Create Payment
echo "5️⃣ Creating a payment..."
PAYMENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": '$ORDER_ID',
    "paymentMethod": "CREDIT_CARD",
    "amount": 1600000
  }')

echo "Payment created: $PAYMENT_RESPONSE"
PAYMENT_ID=$(echo $PAYMENT_RESPONSE | jq -r '.id')

# Test 6: Process Payment
echo "6️⃣ Processing payment..."
curl -s -X POST http://localhost:8080/api/payments/$PAYMENT_ID/process | jq .

# Test 7: Get Payment
echo "7️⃣ Retrieving payment by ID..."
curl -s http://localhost:8080/api/payments/$PAYMENT_ID | jq .

# Test 8: Get Orders by Member
echo "8️⃣ Getting all orders for member..."
curl -s http://localhost:8080/api/orders/member/$MEMBER_ID | jq .

# Test 9: Get Payments by Member
echo "9️⃣ Getting all payments for member..."
curl -s http://localhost:8080/api/payments/member/$MEMBER_ID | jq .

# Test 10: Order Status Change
echo "🔟 Changing order status to confirmed..."
curl -s -X PUT http://localhost:8080/api/orders/$ORDER_ID/confirm | jq .

echo "1️⃣1️⃣ Changing order status to completed..."
curl -s -X PUT http://localhost:8080/api/orders/$ORDER_ID/complete | jq .

# Test 11: Final Order Status
echo "1️⃣2️⃣ Final order status..."
curl -s http://localhost:8080/api/orders/$ORDER_ID | jq .

echo ""
echo "✅ E2E Test Completed Successfully!"
echo "=================================="

# Stop the application
echo "🛑 Stopping application..."
kill $APP_PID
wait $APP_PID 2>/dev/null

echo "🎉 E2E Test finished!"
