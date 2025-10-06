#!/bin/bash

echo "🔧 H2 Database Modes Demo"
echo "========================"

# Function to stop any running application
stop_app() {
    echo "🛑 Stopping any running application..."
    pkill -f "member-order-payment-system" 2>/dev/null || echo "No running application found"
    sleep 2
}

# Function to test H2 connection
test_h2() {
    echo "🧪 Testing H2 connection..."
    sleep 5
    curl -s http://localhost:8080/actuator/health | jq . || echo "❌ Application not responding"
}

echo ""
echo "1️⃣ EMBEDDED MODE (In-Memory) - Current"
echo "======================================"
echo "✅ Data: Stored in memory only"
echo "✅ Persistence: Lost when application stops"
echo "✅ Use case: Development, testing, demos"
echo "✅ URL: jdbc:h2:mem:devdb"
echo ""
echo "Starting embedded mode..."
stop_app
./gradlew bootRun --args='--spring.profiles.active=h2-embedded' --no-daemon &
test_h2

echo ""
echo "2️⃣ GENERIC MODE (File-based)"
echo "============================"
echo "✅ Data: Stored in files on disk"
echo "✅ Persistence: Data survives application restarts"
echo "✅ Use case: Development, small production"
echo "✅ URL: jdbc:h2:file:./data/h2database"
echo ""
echo "Starting generic mode..."
stop_app
./gradlew bootRun --args='--spring.profiles.active=h2-generic' --no-daemon &
test_h2

echo ""
echo "3️⃣ SERVER MODE (Standalone Server)"
echo "=================================="
echo "✅ Data: Stored in server process"
echo "✅ Persistence: Data survives application restarts"
echo "✅ Use case: Development, small production, multiple apps"
echo "✅ URL: jdbc:h2:tcp://localhost:9092/./data/h2server"
echo ""
echo "Note: Server mode requires starting H2 server separately:"
echo "java -cp h2-*.jar org.h2.tools.Server -tcp -tcpPort 9092"
echo ""

echo "🎯 H2 Console Access for all modes:"
echo "=================================="
echo "🌐 URL: http://localhost:8080/h2-console"
echo "👤 Username: sa"
echo "🔑 Password: (empty)"
echo ""
echo "📊 Connection URLs:"
echo "- Embedded: jdbc:h2:mem:devdb"
echo "- Generic:  jdbc:h2:file:./data/h2database"
echo "- Server:   jdbc:h2:tcp://localhost:9092/./data/h2server"
echo ""

echo "🛑 Stopping application..."
stop_app
echo "✅ Demo completed!"
