#!/bin/bash

# LLM Benchmark Agent Runner Script
# This script helps you quickly run the benchmark agent with various configurations

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     LLM Benchmark Agent - Runner          ║${NC}"
echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed or not in PATH${NC}"
    echo "Please install Java 21+ to continue"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo -e "${RED}Error: Java 21 or higher is required${NC}"
    echo "Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

echo -e "${GREEN}✓ Java version check passed${NC}"

# Check if LM Studio is running
if curl -s -f -o /dev/null http://localhost:1234/v1/models 2>/dev/null; then
    echo -e "${GREEN}✓ LM Studio is running${NC}"
else
    echo -e "${RED}⚠ Warning: LM Studio does not appear to be running on localhost:1234${NC}"
    echo "  Please start LM Studio with your MLX model loaded"
fi

# Check if Ollama is running
if curl -s -f -o /dev/null http://localhost:11434/api/tags 2>/dev/null; then
    echo -e "${GREEN}✓ Ollama is running${NC}"
else
    echo -e "${RED}⚠ Warning: Ollama does not appear to be running on localhost:11434${NC}"
    echo "  Please start Ollama with: ollama serve"
fi

# Check for OpenAI API key
if [ -z "$OPENAI_API_KEY" ]; then
    echo -e "${RED}⚠ Warning: OPENAI_API_KEY environment variable not set${NC}"
    echo "  Set it with: export OPENAI_API_KEY=your-key-here"
    echo "  Analysis reports will not be generated without this"
fi

echo ""
echo -e "${BLUE}Starting LLM Benchmark Agent...${NC}"
echo ""

# Build if needed
if [ ! -f "target/llm-benchmark-agent-1.0.0-SNAPSHOT.jar" ]; then
    echo "Building project..."
    ./mvnw clean package -DskipTests
fi

# Run the application
if [ $# -eq 0 ]; then
    echo "Running with default configuration..."
    ./mvnw spring-boot:run
else
    echo "Running with custom prompt: $1"
    ./mvnw spring-boot:run -Dspring-boot.run.arguments="--testPrompt='$1'"
fi
