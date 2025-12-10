# LLM Benchmark Agent

An autonomous agent built with the [Embabel Agent Framework](https://github.com/embabel/embabel-agent-examples) that benchmarks MLX-optimized LLMs (via LM Studio) against standard non-MLX LLMs (via Ollama).

## Overview

This agent automatically:
- Executes test prompts on both MLX and non-MLX models
- Measures execution time and tokens per second
- Compares performance metrics between the two models

## Prerequisites

1. **Java 21+** - Required for running the application
2. **Maven 3.9+** - For building the project (or use included Maven wrapper)
3. **LM Studio** - Running with an MLX-optimized model loaded
4. **Ollama** - Running with a standard model loaded

## Setup

### 1. Install and Configure LM Studio

1. Download [LM Studio](https://lmstudio.ai/)
2. Load an MLX-optimized model (e.g., `qwen3-4b-instruct-2507-mlx`)
3. Start the local server (default: `http://localhost:1234`)
4. Note the exact model name as shown in LM Studio for configuration

### 2. Install and Configure Ollama

1. Install [Ollama](https://ollama.ai/)
2. Pull a model: `ollama pull hopephoto/Qwen3-4B-Instruct-2507_q8:latest` (or your preferred model)
3. Ensure Ollama is running (default: `http://localhost:11434`)

### 3. Configure the Application

Edit `src/main/resources/application.yml` to customize:

```yaml
benchmark:
  mlx-model: qwen3-4b-instruct-2507-mlx
  ollama-model: hopephoto/Qwen3-4B-Instruct-2507_q8:latest
  iterations: 3
```

## Running

### Quick Start

```bash
# Build and run in one command (recommended)
mvn clean spring-boot:run

# Or use Maven wrapper
./mvnw clean spring-boot:run
```

Once the application starts, you'll see the Embabel shell prompt (`embabel>`). To run the benchmark, type:

```
x "run benchmark"
```

The agent will then execute the benchmark test using the configured models and default prompt from `application.yml`.

## Agent Workflow

The benchmark agent follows this autonomous workflow:

1. **Prepare Benchmark** (`prepareBenchmark`)
   - Initializes test configuration
   - Sets up prompts and model parameters

2. **Test MLX Model** (`testMlxModel`)
   - Executes benchmark test on LM Studio
   - Measures execution time and tokens per second

3. **Test Ollama Model** (`testOllamaModel`)
   - Executes benchmark test on Ollama
   - Measures execution time and tokens per second

4. **Compare Results** (`compareResults`)
   - Calculates speed differences
   - Determines which model is faster
   - Compares tokens per second performance

## Example Output

```
=== Preparing LLM Benchmark ===
MLX Model (LM Studio): qwen3-4b-instruct-2507-mlx
Ollama Model: hopephoto/Qwen3-4B-Instruct-2507_q8:latest
Iterations: 3
Using default test prompt: Document:...

--- Testing MLX Model: 'qwen3-4b-instruct-2507-mlx' ---
Execution time: 18523ms

--- Testing Ollama Model: 'hopephoto/Qwen3-4B-Instruct-2507_q8:latest' ---
Execution time: 42817ms

----------- Comparing Results -----------
Faster model: MLX
Tokens per second difference: 131.13%
Speed difference: 56.74%
-----------------------------------------
```

## Configuration Options

### Model Selection

Configure different models in `application.yml`:

```yaml
benchmark:
  mlx-model: your-mlx-model-name
  ollama-model: your-ollama-model-name
```

### Test Parameters

```yaml
benchmark:
  iterations: 3           # Number of test iterations
```

### Custom Test Prompt

Modify the `default-prompt` in `application.yml` or edit `BenchmarkProperties.kt` to set your own test prompt. The default prompt is a complex document with analytical questions designed to test reasoning capabilities.

## Project Structure

```
llm-benchmark-agent/
├── src/main/
│   ├── kotlin/com/embabel/benchmark/
│   │   ├── BenchmarkApplication.kt      # Spring Boot entry point
│   │   ├── BenchmarkAgent.kt            # Agent with 4 @Action methods
│   │   ├── config/
│   │   │   └── BenchmarkProperties.kt   # Configuration properties
│   │   └── model/
│   │       └── BenchmarkResults.kt      # Data models
│   └── resources/
│       └── application.yml              # Configuration file
├── pom.xml                              # Maven dependencies
├── README.md                            # This file
└── .gitignore
```

## Architecture

The agent uses the Embabel Agent Framework with:

- **@Agent**: Marks the autonomous agent class
- **@Action**: Defines discrete agent actions
- **@AchievesGoal**: Marks the terminal successful action
- **OperationContext**: Provides access to AI models via `context.ai()`

### Key Components

- `BenchmarkAgent.kt`: Main agent logic with 4 actions (prepareBenchmark, testMlxModel, testOllamaModel, compareResults)
- `BenchmarkProperties.kt`: Configuration management (mlxModel, ollamaModel, iterations, defaultPrompt)
- `BenchmarkResults.kt`: Data models (TestResult, ModelType, ComparisonResult, TestConfiguration)
- `BenchmarkApplication.kt`: Spring Boot entry point

### Extending the Agent

**Add a new model provider:**
1. Configure provider in `application.yml`
2. Add new test action in `BenchmarkAgent.kt`
3. Update `ModelType` enum in `BenchmarkResults.kt`

**Add custom metrics:**
1. Extend `TestResult` data class with new fields
2. Calculate metrics in test actions
3. Update `compareResults()` to include new metrics

**Change output format:**
1. Modify `compareResults()` to output JSON, CSV, or HTML
2. Use Spring profiles for format selection

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

The OpenAI API key is optional and only needed if you want to use OpenAI models. The benchmark works with locally running LM Studio and Ollama models.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

## License

This project is provided as-is for educational and testing purposes.

## References

- [Embabel Agent Framework](https://github.com/embabel/embabel-agent-examples)
- [LM Studio](https://lmstudio.ai/)
- [Ollama](https://ollama.ai/)
- [MLX Optimizations](https://github.com/ml-explore/mlx)
