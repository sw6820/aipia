#!/bin/bash

echo "🚀 Starting H2 Server Mode"
echo "=========================="

# Check if H2 jar is available
H2_JAR=$(find ~/.gradle/caches -name "h2-*.jar" | head -1)

if [ -z "$H2_JAR" ]; then
    echo "❌ H2 jar not found. Please run: ./gradlew build first"
    exit 1
fi

echo "📦 Found H2 jar: $H2_JAR"
echo "🌐 Starting H2 server on port 9092..."
echo "📁 Data directory: ./data/h2server"
echo ""
echo "🔗 Connection URL: jdbc:h2:tcp://localhost:9092/./data/h2server"
echo "🌐 Web Console: http://localhost:8082"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Create data directory if it doesn't exist
mkdir -p ./data/h2server

# Start H2 server
java -cp "$H2_JAR" org.h2.tools.Server \
    -tcp -tcpPort 9092 \
    -tcpAllowOthers \
    -web -webPort 8082 \
    -webAllowOthers \
    -baseDir ./data/h2server
