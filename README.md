# LLM Benchmark Agent

An autonomous agent built with the [Embabel Agent Framework](https://github.com/embabel/embabel-agent-examples) that benchmarks MLX-optimized LLMs (via LM Studio) against standard non-MLX LLMs (via Ollama).

## Overview

This agent automatically:
- Executes test prompts on both MLX and non-MLX models
- Measures execution time and tokens per second
- Compares performance metrics
- Generates comprehensive benchmark reports with AI-powered analysis

## Prerequisites

1. **Java 21+** - Required for running the application
2. **Maven 3.9+** - For building the project (or use included Maven wrapper)
3. **LM Studio** - Running with an MLX-optimized model loaded
4. **Ollama** - Running with a standard model loaded
5. **OpenAI API Key** - For generating analysis reports (optional but recommended)

## Setup

### 1. Install and Configure LM Studio

1. Download [LM Studio](https://lmstudio.ai/)
2. Load an MLX-optimized model (e.g., `mlx-community/Qwen2.5-7B-Instruct-4bit`)
3. Start the local server (default: `http://localhost:1234`)
4. Note the model name for configuration

### 2. Install and Configure Ollama

1. Install [Ollama](https://ollama.ai/)
2. Pull a model: `ollama pull qwen2.5:7b` (or your preferred model)
3. Ensure Ollama is running (default: `http://localhost:11434`)

### 3. Set Environment Variables

```bash
# Required for analysis reports
export OPENAI_API_KEY=your-api-key-here

# Optional: Override default configurations
export BENCHMARK_LM_STUDIO_MODEL_NAME=your-mlx-model-name
export BENCHMARK_OLLAMA_MODEL_NAME=your-ollama-model-name
```

### 4. Configure the Application

Edit `src/main/resources/application.yml` to customize:

```yaml
benchmark:
  lm-studio:
    base-url: http://localhost:1234/v1
    model-name: mlx-community/Qwen2.5-7B-Instruct-4bit

  ollama:
    base-url: http://localhost:11434
    model-name: qwen2.5:7b

  warmup-runs: 1
  iterations: 1
```

## Building

```bash
# Using Maven wrapper (recommended)
./mvnw clean install

# Or with installed Maven
mvn clean install
```

## Running

### Interactive Shell Mode (Default)

```bash
# Using Maven
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/llm-benchmark-agent-1.0.0-SNAPSHOT.jar
```

Once in the shell, you can:
- Run default benchmark: `run benchmark`
- Use custom prompt: `run benchmark with prompt "Your custom test prompt here"`

### Command Line Arguments

```bash
# With custom test prompt
./mvnw spring-boot:run -Dspring-boot.run.arguments="--testPrompt='Explain how transformers work in machine learning.'"
```

## Agent Workflow

The benchmark agent follows this autonomous workflow:

1. **Prepare Benchmark** (`prepareBenchmark`)
   - Initializes test configuration
   - Sets up prompts and model parameters

2. **Test MLX Model** (`testMlxModel`)
   - Performs warmup runs on LM Studio
   - Executes benchmark test
   - Measures execution time

3. **Test Ollama Model** (`testOllamaModel`)
   - Performs warmup runs on Ollama
   - Executes benchmark test
   - Measures execution time

4. **Compare Results** (`compareResults`)
   - Calculates speed differences
   - Generates AI-powered performance analysis

5. **Generate Report** (`generateReport`)
   - Creates comprehensive benchmark report
   - Provides recommendations
   - Outputs final results

## Example Output

```
=== Preparing LLM Benchmark ===
MLX Model (LM Studio): mlx-community/Qwen2.5-7B-Instruct-4bit
Ollama Model: qwen2.5:7b
Iterations: 1
Warmup runs: 1

--- Testing MLX Model (mlx-community/Qwen2.5-7B-Instruct-4bit) ---
Performing 1 warmup run(s)...
Execution time: 2345ms
Response preview: Quantum computing is a revolutionary approach to computation that...

--- Testing Ollama Model (qwen2.5:7b) ---
Performing 1 warmup run(s)...
Execution time: 3821ms
Response preview: Quantum computing represents a paradigm shift in how we process...

--- Comparing Results ---
Speed difference: 38.62%
Faster model: MLX

======================================================================
   LLM BENCHMARK REPORT: MLX vs Standard Models
======================================================================

TEST PROMPT:
  Explain quantum computing in simple terms.

RESULTS:
  MLX Model (mlx-community/Qwen2.5-7B-Instruct-4bit):
    - Time: 2345ms
    - Speed: 45.23 tokens/sec

  Ollama Model (qwen2.5:7b):
    - Time: 3821ms
    - Speed: 27.81 tokens/sec

PERFORMANCE DIFFERENCE:
  MLX faster by 38.62%

SUMMARY:
  The MLX-optimized model demonstrated significantly better performance...

RECOMMENDATIONS:
  1. Use MLX models for production workloads on Apple Silicon...
  2. Standard Ollama models are suitable for development and testing...
  3. Consider MLX optimization for latency-sensitive applications...

======================================================================
```

## Configuration Options

### Model Selection

Configure different models in `application.yml`:

```yaml
benchmark:
  lm-studio:
    model-name: mlx-community/Meta-Llama-3.1-8B-Instruct-4bit
  ollama:
    model-name: llama3.1:8b
```

### Test Parameters

```yaml
benchmark:
  warmup-runs: 2          # Number of warmup runs before benchmark
  iterations: 3           # Number of test iterations
  timeout-seconds: 120    # Timeout for each LLM call
```

### Custom Prompts

Add your own test prompts:

```yaml
benchmark:
  default-prompts:
    - "Your custom prompt 1"
    - "Your custom prompt 2"
    - "Your custom prompt 3"
```

## Architecture

The agent uses the Embabel Agent Framework with:

- **@Agent**: Marks the autonomous agent class
- **@Action**: Defines discrete agent actions
- **@AchievesGoal**: Marks the terminal successful action
- **ActionContext**: Provides access to AI models and tools

### Key Components

- `BenchmarkAgent.kt`: Main agent logic and workflow
- `BenchmarkProperties.kt`: Configuration management
- `BenchmarkResults.kt`: Data models for test results
- `BenchmarkApplication.kt`: Spring Boot entry point

## Troubleshooting

### LM Studio Connection Issues

```bash
# Check if LM Studio is running
curl http://localhost:1234/v1/models

# Verify model is loaded in LM Studio UI
```

### Ollama Connection Issues

```bash
# Check if Ollama is running
ollama list

# Test Ollama API
curl http://localhost:11434/api/tags
```

### Missing OpenAI API Key

If you don't have an OpenAI API key, you can:
1. Use a local model for analysis by changing `analysis-model` in `application.yml`
2. Remove the analysis steps from the agent workflow

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is provided as-is for educational and testing purposes.

## References

- [Embabel Agent Framework](https://github.com/embabel/embabel-agent-examples)
- [LM Studio](https://lmstudio.ai/)
- [Ollama](https://ollama.ai/)
- [MLX Optimizations](https://github.com/ml-explore/mlx)
