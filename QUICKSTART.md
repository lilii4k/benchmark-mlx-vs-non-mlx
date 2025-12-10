# Quick Start Guide

Get your LLM benchmark running in 5 minutes!

## Step 1: Prerequisites Check

Make sure you have:
- [ ] Java 21+ installed (`java -version`)
- [ ] LM Studio installed and running
- [ ] Ollama installed and running
- [ ] OpenAI API key (optional, for analysis)

## Step 2: Start Your LLM Services

### LM Studio (MLX Model)
1. Open LM Studio
2. Download an MLX-optimized model:
   - Search for `mlx-community/Qwen2.5-7B-Instruct-4bit`
   - Or any other MLX model
3. Load the model
4. Click "Start Server" (should run on `http://localhost:1234`)

### Ollama (Standard Model)
```bash
# Pull a model if you haven't already
ollama pull qwen2.5:7b

# Ollama should auto-start, but if not:
ollama serve
```

## Step 3: Configure the Agent

### Set your OpenAI API key (for analysis):
```bash
export OPENAI_API_KEY=sk-your-key-here
```

### Update model names (if different):
Edit `src/main/resources/application.yml`:
```yaml
benchmark:
  lm-studio:
    model-name: your-mlx-model-name  # Update this
  ollama:
    model-name: your-ollama-model    # Update this
```

## Step 4: Run the Agent

### Option A: Use the helper script (easiest)
```bash
chmod +x run.sh
./run.sh
```

### Option B: Use Maven directly
```bash
./mvnw spring-boot:run
```

### Option C: With a custom prompt
```bash
./run.sh "Explain machine learning in simple terms"
```

## Step 5: Interpret Results

You'll see output like:
```
=== Preparing LLM Benchmark ===
MLX Model: mlx-community/Qwen2.5-7B-Instruct-4bit
Ollama Model: qwen2.5:7b

--- Testing MLX Model ---
Execution time: 2345ms
Speed: 45.23 tokens/sec

--- Testing Ollama Model ---
Execution time: 3821ms
Speed: 27.81 tokens/sec

--- Final Report ---
MLX faster by 38.62%
```

## Common Issues

### "Connection refused" errors
- **LM Studio**: Make sure the server is started in LM Studio
- **Ollama**: Run `ollama serve` in a terminal

### "Model not found"
- **LM Studio**: Check the exact model name in LM Studio's UI
- **Ollama**: Run `ollama list` to see available models

### No analysis in report
- Set your `OPENAI_API_KEY` environment variable
- Or change `analysis-model` in `application.yml` to a local model

## Next Steps

1. **Try different prompts**: Test with various prompt types (coding, creative, analytical)
2. **Adjust iterations**: Increase `iterations` in `application.yml` for more accurate results
3. **Compare models**: Try different model sizes and quantizations
4. **Export results**: Modify the agent to save results to CSV or JSON

## Example Test Prompts

Try these prompts to test different scenarios:

```bash
# Short response
./run.sh "What is 2+2?"

# Medium response
./run.sh "Explain how a car engine works"

# Long response
./run.sh "Write a detailed tutorial on Python decorators with examples"

# Code generation
./run.sh "Write a binary search algorithm in Python with comments"
```

## Understanding the Results

### Key Metrics
- **Execution Time (ms)**: Total time to generate the response
- **Tokens/sec**: Throughput measure (higher is better)
- **Speed Difference**: Percentage improvement of faster model

### What to Look For
- **Consistent performance**: MLX should generally be faster on Apple Silicon
- **Quality differences**: Compare response quality between models
- **Scalability**: Test with longer prompts to see performance curves

## Customization

### Add your own test prompts
Edit `application.yml`:
```yaml
benchmark:
  default-prompts:
    - "Your custom prompt 1"
    - "Your custom prompt 2"
```

### Change warmup settings
```yaml
benchmark:
  warmup-runs: 2  # More warmup = more consistent results
  iterations: 3   # More iterations = better average
```

## Getting Help

- Check the full [README.md](README.md) for detailed documentation
- Review [Embabel documentation](https://github.com/embabel/embabel-agent-examples)
- Check your LM Studio and Ollama logs for errors

Happy benchmarking! 🚀
