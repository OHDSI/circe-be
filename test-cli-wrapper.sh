#!/bin/bash
# Test script for CLI wrapper functionality
set -e

echo "Testing CLI Wrapper Functionality"
echo "================================="

# Ensure the JAR exists
if [ ! -f "target/circe-cli.jar" ]; then
    echo "Error: JAR file not found. Please run 'mvn package -DskipTests' first."
    exit 1
fi

echo ""
echo "1. Testing version command..."
java -jar target/circe-cli.jar version
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Version command test passed"
else
    echo "❌ Version command test failed with exit code $EXIT_CODE"
    exit 1
fi

echo ""
echo "2. Testing usage display (no arguments)..."
java -jar target/circe-cli.jar
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Usage display test passed"
else
    echo "❌ Usage display test failed with exit code $EXIT_CODE"
    exit 1
fi

echo ""
echo "3. Testing invalid command..."
java -jar target/circe-cli.jar invalid-command
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Invalid command test passed"
else
    echo "❌ Invalid command test failed with exit code $EXIT_CODE"
    exit 1
fi

echo ""
echo "4. Testing cohort validation with invalid JSON..."
java -jar target/circe-cli.jar validate-cohort "invalid-json"
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Invalid cohort JSON test passed"
else
    echo "❌ Invalid cohort JSON test failed with exit code $EXIT_CODE"
    exit 1
fi

echo ""
echo "5. Testing concept set validation with invalid JSON..."
java -jar target/circe-cli.jar validate-conceptset "invalid-json"
EXIT_CODE=$?
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Invalid concept set JSON test passed"
else
    echo "❌ Invalid concept set JSON test failed with exit code $EXIT_CODE"
    exit 1
fi

echo ""
echo "✅ All CLI wrapper tests passed!"
echo ""