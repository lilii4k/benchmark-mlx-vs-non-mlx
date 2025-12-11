# LLM Benchmark Agent

An autonomous agent built with the [Embabel Agent Framework](https://github.com/embabel/embabel-agent-examples) that benchmarks MLX-optimized LLMs (via LM Studio) against standard non-MLX LLMs (via Ollama).

## Overview

This agent automatically:
- Runs multiple iterations of test prompts on both MLX and non-MLX models
- Measures execution time and tokens per second across iterations
- Uses a judge model to evaluate response quality
- Compares both speed and quality metrics between the two models

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
  judge-model: openai/gpt-oss-20b  # Model used to evaluate response quality
  iterations: 100  # Number of test iterations per model
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

2. **Run Complete Benchmark** (`runCompleteBenchmark`)
   - For each iteration (1 to N):
     - Tests MLX model with the prompt
     - Tests Ollama model with the same prompt
     - Judges the pair of responses immediately (avoids context overflow)
     - Stores individual scores, times, and token rates
   - After all iterations:
     - Calculates average execution times
     - Calculates average tokens per second
     - Calculates average quality scores
     - Counts wins for each model
     - Displays comprehensive final summary

## Example Output

```
========== Preparing LLM Benchmark ==========
MLX Model (LM Studio): qwen3-4b-instruct-2507-mlx
Non-MLX Model (Ollama): hopephoto/Qwen3-4B-Instruct-2507_q8:latest
Judge Model: openai/gpt-oss-20b
Iterations: 3
Using default test prompt: Document:...

--------- Iteration 1/3 ---------

Testing MLX model: 'qwen3-4b-instruct-2507-mlx'...

Finished (17034ms, 79.08 tokens/sec).

Testing Ollama model: 'hopephoto/Qwen3-4B-Instruct-2507_q8:latest'...

Finished (19026ms, 56.45 tokens/sec).

Comparing quality of responses...

Judgement Complete - Winner: MLX

--------- Iteration 2/3 ---------

Testing MLX model: 'qwen3-4b-instruct-2507-mlx'...

Finished (15662ms, 78.66 tokens/sec).

Testing Ollama model: 'hopephoto/Qwen3-4B-Instruct-2507_q8:latest'...

Finished (16753ms, 63.81 tokens/sec).

Comparing quality of responses...

Judgement Complete - Winner: Ollama

--------- Iteration 3/3 ---------

Testing MLX model: 'qwen3-4b-instruct-2507-mlx'...

Finished (14479ms, 75.83 tokens/sec).

Testing Ollama model: 'hopephoto/Qwen3-4B-Instruct-2507_q8:latest'...

Finished (16064ms, 66.11 tokens/sec).

Comparing quality of responses...

Judgement Complete - Winner: Ollama


======================================================================
FINAL BENCHMARK SUMMARY
======================================================================

[qwen3-4b-instruct-2507-mlx] (MLX) vs. [hopephoto/Qwen3-4B-Instruct-2507_q8:latest] (non-MLX)

SPEED METRICS:
  Faster Model: MLX (9.00% faster)
  Average Time (MLX): 15725ms
  Average Time (non-MLX): 17281ms
  Average TPS (MLX): 77.86 tokens/sec
  Average TPS (non-MLX): 62.12 tokens/sec

QUALITY METRICS:
  Best Quality: Ollama
  Average Score (MLX): 71.67/100
  Average Score (non-MLX): 80.00/100
  Wins (MLX): 1
  Wins (non-MLX): 2
  Ties: 0

======================================================================
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
  iterations: 100         # Number of test iterations per model
  judge-model: openai/gpt-oss-20b  # Model used to evaluate quality
```

**Iterations**: The benchmark runs each model multiple times (default: 100). For each iteration, both models generate a response to the same prompt, and the judge immediately evaluates the pair. This approach:
- Prevents context window overflow by judging one pair at a time
- Allows for per-iteration performance tracking
- Calculates reliable average metrics across all iterations
- More iterations provide more reliable statistics but take longer to complete

**Judge Model**: An LLM that evaluates the quality of responses from both models for each iteration, scoring them on accuracy, completeness, reasoning quality, and clarity. The judge assigns scores (0-100) and declares a winner for each iteration.

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

- `BenchmarkAgent.kt`: Main agent logic with 2 actions (prepareBenchmark, runCompleteBenchmark)
- `BenchmarkProperties.kt`: Configuration management (mlxModel, ollamaModel, judgeModel, iterations, defaultPrompt)
- `BenchmarkResults.kt`: Data models (TestConfiguration, IterationData, JudgeResult, AggregatedBenchmarkResult)
- `BenchmarkApplication.kt`: Spring Boot entry point

### Performance Metrics

The agent tracks for each iteration and calculates averages:
- **Execution Time**: Individual and average time per iteration (milliseconds)
- **Tokens Per Second**: Individual and average throughput based on response length
- **Quality Scores**: Individual and average scores (0-100) assigned by the judge model
- **Win Counts**: Number of times each model wins in quality judgments
- **Speed Difference**: Percentage difference between average model execution times

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
