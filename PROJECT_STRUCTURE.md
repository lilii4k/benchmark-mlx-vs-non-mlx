# Project Structure

## Overview

```
llm-benchmark-agent/
├── src/
│   └── main/
│       ├── kotlin/com/embabel/benchmark/
│       │   ├── BenchmarkApplication.kt      # Spring Boot entry point
│       │   ├── BenchmarkAgent.kt            # Main agent with workflow logic
│       │   ├── config/
│       │   │   └── BenchmarkProperties.kt   # Configuration properties
│       │   └── model/
│       │       └── BenchmarkResults.kt      # Data classes for results
│       └── resources/
│           ├── application.yml              # Main configuration
│           └── application-example.yml      # Example/template config
├── pom.xml                                  # Maven dependencies
├── run.sh                                   # Helper script to run agent
├── README.md                                # Full documentation
├── QUICKSTART.md                            # Quick start guide
└── .gitignore                               # Git ignore rules
```

## File Descriptions

### Core Application Files

#### `BenchmarkApplication.kt`
- Spring Boot application entry point
- Configures component scanning for Embabel agent discovery
- Main method to launch the application

#### `BenchmarkAgent.kt`
The heart of the application. Contains:
- `@Agent` annotation marking it as an autonomous agent
- Five `@Action` methods implementing the workflow:
  1. `prepareBenchmark()` - Initialize test configuration
  2. `testMlxModel()` - Run benchmark on MLX model (LM Studio)
  3. `testOllamaModel()` - Run benchmark on Ollama model
  4. `compareResults()` - Analyze performance differences
  5. `generateReport()` - Create final report (marked with `@AchievesGoal`)
- Helper methods for timing and formatting

### Configuration

#### `BenchmarkProperties.kt`
Spring `@ConfigurationProperties` class containing:
- Default test prompts
- LM Studio configuration (URL, model name)
- Ollama configuration (URL, model name)
- Performance tuning parameters (warmup, iterations, timeouts)
- Analysis model selection

#### `application.yml`
Main configuration file with:
- Embabel agent settings
- Provider configurations (LM Studio, Ollama, OpenAI)
- Benchmark parameters
- Logging configuration

### Data Models

#### `BenchmarkResults.kt`
Data classes for the agent workflow:
- `TestResult` - Single test execution result with timing data
- `ModelType` - Enum for MLX vs Ollama
- `ComparisonResult` - Performance comparison between models
- `BenchmarkReport` - Final comprehensive report
- `TestConfiguration` - Test setup parameters

### Build & Dependencies

#### `pom.xml`
Maven configuration with:
- Java 21 and Kotlin dependencies
- Embabel agent framework starters:
  - `embabel-agent-starter` - Core framework
  - `embabel-agent-starter-shell` - Interactive CLI
  - `embabel-agent-starter-ollama` - Ollama integration
  - `embabel-agent-starter-openai` - OpenAI/LM Studio integration
- Spring Boot configuration processor
- Kotlin compiler plugin with Spring support

### Helper Files

#### `run.sh`
Convenience script that:
- Checks prerequisites (Java version, LM Studio, Ollama)
- Builds the project if needed
- Runs the agent with optional custom prompts

#### `README.md`
Complete documentation including:
- Prerequisites and setup instructions
- Configuration options
- Running instructions
- Example output
- Troubleshooting guide

#### `QUICKSTART.md`
Abbreviated guide to get running quickly with minimal configuration.

## Agent Workflow

The agent follows this execution path:

```
User Input
    ↓
prepareBenchmark()
    ↓
    ├─→ testMlxModel() ────┐
    │                      ↓
    └─→ testOllamaModel() ─┤
                           ↓
                    compareResults()
                           ↓
                    generateReport()
                           ↓
                    (Achieves Goal)
```

## Key Embabel Concepts Used

### Annotations
- `@Agent`: Declares the autonomous agent
- `@Action`: Marks methods as discrete agent actions
- `@AchievesGoal`: Indicates terminal success action

### ActionContext
Provides access to:
- `context.ai()` - AI model interaction
- `.model()` - Select specific model
- `.provider()` - Choose provider (lmstudio, ollama, openai)
- `.prompt()` - Set the prompt
- `.temperature()` - Control randomness
- `.maxTokens()` - Limit response length
- `.call()` - Execute the request

### Configuration
- Spring `@ConfigurationProperties` for type-safe config
- Profile-based configuration support
- Environment variable integration

## Data Flow

1. **Input**: User provides test prompt (optional)
2. **Configuration**: `TestConfiguration` created with prompts and model names
3. **Execution**: Both models tested independently
4. **Results**: `TestResult` objects with timing and response data
5. **Analysis**: `ComparisonResult` with performance metrics
6. **Output**: `BenchmarkReport` with summary and recommendations

## Extension Points

### Adding New Models
1. Add provider configuration in `application.yml`
2. Create new test action in `BenchmarkAgent.kt`
3. Update `ModelType` enum if needed

### Custom Metrics
1. Extend `TestResult` data class
2. Add measurement logic in test actions
3. Update comparison and reporting logic

### Different Report Formats
1. Add new action method marked with `@AchievesGoal`
2. Implement custom formatting (JSON, CSV, HTML)
3. Use Spring profiles to select report type

## Testing Strategy

### Manual Testing
1. Start LM Studio with an MLX model
2. Start Ollama with a standard model
3. Run the agent and verify:
   - Both models are tested
   - Timing is accurate
   - Report is generated

### Integration Testing
Consider adding tests for:
- Agent workflow execution
- Configuration loading
- Provider connectivity
- Result calculations

### Performance Testing
- Run with multiple iterations
- Test various prompt lengths
- Compare different model sizes

## Dependencies

### Required at Runtime
- Java 21+
- LM Studio (running with MLX model)
- Ollama (running with standard model)
- OpenAI API key (for analysis, optional)

### Maven Dependencies
- Spring Boot 3.2.2
- Kotlin 1.9.22
- Embabel Agent Framework 0.3.1-SNAPSHOT

## Configuration Profiles

You can create environment-specific configs:
- `application.yml` - Base configuration
- `application-local.yml` - Local development overrides
- `application-prod.yml` - Production settings

Activate with:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Logging

Logging is configured in `application.yml`:
- `DEBUG` level for `com.embabel.benchmark` - Agent execution details
- `INFO` level for `com.embabel` - Framework operations
- `WARN` level for root - General application logs

Customize log levels for troubleshooting specific issues.
