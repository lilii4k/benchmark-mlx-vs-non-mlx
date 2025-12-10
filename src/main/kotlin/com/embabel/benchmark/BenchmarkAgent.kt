package com.embabel.benchmark

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.benchmark.config.BenchmarkProperties
import com.embabel.benchmark.model.*
import com.embabel.common.ai.model.LlmOptions
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.system.measureTimeMillis

@Agent(
    description = """
        Autonomous agent that benchmarks MLX-optimized LLMs from LM Studio
        against standard non-MLX LLMs from Ollama. Measures execution speed,
        tokens per second, and provides detailed performance comparisons.
    """
)
@Component
class BenchmarkAgent(
    private val properties: BenchmarkProperties
) {

    @Action
    fun prepareBenchmark(context: OperationContext): TestConfiguration {
        println("\n=== Preparing LLM Benchmark ===")
        println("MLX Model (LM Studio): ${properties.mlxModel}")
        println("Ollama Model: ${properties.ollamaModel}")
        println("Iterations: ${properties.iterations}")

        println("Using default test prompt: ${properties.defaultPrompt.take(60)}...")
        val prompts = listOf(properties.defaultPrompt)
        return TestConfiguration(
            prompts = prompts,
            mlxModelName = properties.mlxModel,
            ollamaModelName = properties.ollamaModel,
            iterations = properties.iterations,
        )
    }

    @Action
    fun testMlxModel(context: OperationContext, config: TestConfiguration): MlxTestResult {
        println("\n--- Testing MLX Model: '${config.mlxModelName}' ---")

        // Actual benchmark test (using first prompt)
        val testPrompt = config.prompts.first()
        var response = ""

        val executionTime = measureTimeMillis {
            response = context.ai()
                .withLlm(LlmOptions.withModel(config.mlxModelName).withTemperature(0.2))
                .generateText(testPrompt)
        }

        println("Execution time: ${executionTime}ms")

        val testResult = TestResult(
            modelName = config.mlxModelName,
            modelType = ModelType.MLX_LM_STUDIO,
            prompt = testPrompt,
            response = response,
            executionTimeMs = executionTime,
            tokensPerSecond = calculateTokensPerSecond(response, executionTime)
        )

        return MlxTestResult(testResult)
    }

    @Action
    fun testOllamaModel(context: OperationContext, config: TestConfiguration): OllamaTestResult {
        println("\n--- Testing Ollama Model: '${config.ollamaModelName}' ---")
        // Actual benchmark test
        val testPrompt = config.prompts.first()
        var response = ""

        val executionTime = measureTimeMillis {
            response = context.ai()
                .withLlm(LlmOptions.withModel(config.ollamaModelName).withTemperature(0.2))
                .generateText(testPrompt)
        }

        println("Execution time: ${executionTime}ms")

        val testResult = TestResult(
            modelName = config.ollamaModelName,
            modelType = ModelType.OLLAMA_STANDARD,
            prompt = testPrompt,
            response = response,
            executionTimeMs = executionTime,
            tokensPerSecond = calculateTokensPerSecond(response, executionTime)
        )

        return OllamaTestResult(testResult)
    }

    @Action
    @AchievesGoal(
        description = "Compare results of the MLX and non-MLX benchmark tests to see which is faster.")
    fun compareResults(
        context: OperationContext,
        mlxTestResult: MlxTestResult,
        ollamaTestResult: OllamaTestResult
    ): ComparisonResult {
        println("\n----------- Comparing Results -----------")

        val mlxResult = mlxTestResult.result
        val ollamaResult = ollamaTestResult.result

        val speedDifference = if (mlxResult.executionTimeMs < ollamaResult.executionTimeMs) {
            ((ollamaResult.executionTimeMs - mlxResult.executionTimeMs).toDouble() /
                ollamaResult.executionTimeMs) * 100
        } else {
            ((mlxResult.executionTimeMs - ollamaResult.executionTimeMs).toDouble() /
                mlxResult.executionTimeMs) * 100
        }

        val mlxFaster = mlxResult.executionTimeMs < ollamaResult.executionTimeMs
        val mlxTps = mlxResult.tokensPerSecond ?: 0.0
        val ollamaTps = ollamaResult.tokensPerSecond ?: 0.0
        val tpsDifference = if (mlxTps > ollamaTps) {
            ((mlxTps - ollamaTps) / ollamaTps) * 100
        } else {
            ((ollamaTps - mlxTps) / mlxTps) * 100
        }

        println("Faster model: ${if (mlxFaster) "MLX" else "Ollama"}")
        println("Tokens per second difference: ${String.format("%.2f", tpsDifference)}%")
        println("Speed difference: ${String.format("%.2f", speedDifference)}%")
        println("-----------------------------------------")

        return ComparisonResult(
            mlxResult = mlxResult,
            ollamaResult = ollamaResult,
            speedDifferencePercent = speedDifference,
            mlxFaster = mlxFaster
        )
    }

    private fun calculateTokensPerSecond(response: String, timeMs: Long): Double {
        // Rough estimation: ~4 chars per token
        val estimatedTokens = response.length / 4
        val timeSeconds = timeMs / 1000.0
        return if (timeSeconds > 0) estimatedTokens / timeSeconds else 0.0
    }
}
