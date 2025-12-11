package com.embabel.benchmark

import com.embabel.agent.api.annotation.*
import com.embabel.agent.api.common.OperationContext
import com.embabel.agent.api.common.create
import com.embabel.benchmark.config.BenchmarkProperties
import com.embabel.benchmark.model.*
import com.embabel.common.ai.model.LlmOptions
import org.springframework.stereotype.Component
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
        println("Judge Model: ${properties.judgeModel}")
        println("Iterations: ${properties.iterations}")

        println("Using default test prompt: ${properties.defaultPrompt.take(60)}...")
        return TestConfiguration(
            prompt = properties.defaultPrompt,
            mlxModelName = properties.mlxModel,
            ollamaModelName = properties.ollamaModel,
            judgeModelName = properties.judgeModel,
            iterations = properties.iterations,
        )
    }

    @Action
    fun testMlxModel(context: OperationContext, config: TestConfiguration): MlxTestResult {
        println("\n--- Testing MLX Model: '${config.mlxModelName}' with ${config.iterations} iterations ---")

        val testPrompt = config.prompt
        var lastResponse = ""

        val totalTime = measureTimeMillis {
            for (i in 1..config.iterations) {
                print("Iteration $i/${config.iterations}... ")

                lastResponse = context.ai()
                    .withLlm(LlmOptions.withModel(config.mlxModelName).withTemperature(0.2))
                    .generateText(testPrompt)
            }
        }

        val avgTime = totalTime / config.iterations
        val tokensPerSec = calculateTokensPerSecond(lastResponse, avgTime)

        val testResult = TestResult(
            modelName = config.mlxModelName,
            modelType = ModelType.MLX_LM_STUDIO,
            prompt = testPrompt,
            response = lastResponse,
            executionTimeMs = avgTime,
            tokensPerSecond = tokensPerSec,
            iterationCount = config.iterations
        )
        return MlxTestResult(testResult)
    }

    @Action
    fun testOllamaModel(context: OperationContext, config: TestConfiguration): OllamaTestResult {
        println("\n--- Testing Ollama Model: '${config.ollamaModelName}' with ${config.iterations} iterations ---")

        val testPrompt = config.prompt
        var lastResponse = ""

        val totalTime = measureTimeMillis {
            for (i in 1..config.iterations) {
                print("Iteration $i/${config.iterations}... ")

                lastResponse = context.ai()
                    .withLlm(LlmOptions.withModel(config.ollamaModelName).withTemperature(0.2))
                    .generateText(testPrompt)
            }
        }

        val avgTime = totalTime / config.iterations
        val tokensPerSec = calculateTokensPerSecond(lastResponse, avgTime)

        val testResult = TestResult(
            modelName = config.ollamaModelName,
            modelType = ModelType.OLLAMA_STANDARD,
            prompt = testPrompt,
            response = lastResponse,
            executionTimeMs = avgTime,
            tokensPerSecond = tokensPerSec,
            iterationCount = config.iterations
        )
        return OllamaTestResult(testResult)
    }

    @Action
    fun judgeQualityComparison(
        context: OperationContext,
        config: TestConfiguration,
        mlxTestResult: MlxTestResult,
        ollamaTestResult: OllamaTestResult
    ): JudgeResult {
        println("\n--- Evaluating Quality - Using Model: ${config.judgeModelName} ---")

        val mlxResponse = mlxTestResult.result.response
        val ollamaResponse = ollamaTestResult.result.response
        val originalPrompt = mlxTestResult.result.prompt

        val judgePrompt = """
You are an expert evaluator comparing two AI model responses. Evaluate both responses based on:
1. Factual accuracy and correctness
2. Completeness (how well all questions are answered)
3. Reasoning quality and logical coherence
4. Clarity and organization

Original Prompt:
$originalPrompt

Response A (MLX Model):
$mlxResponse

Response B (Ollama Model):
$ollamaResponse

Provide your evaluation in this EXACT format:
MLX_SCORE: [score 0-100]
OLLAMA_SCORE: [score 0-100]
WINNER: [either "MLX" or "Ollama" or "Tie"]
REASONING: [Brief explanation of your judgment (3 sentences max.)]
        """.trimIndent()

        val judgeResponse = context.ai()
            .withLlm(LlmOptions.withModel(config.judgeModelName).withTemperature(0.3))
            .generateText(judgePrompt)

        val mlxScore = extractScore(judgeResponse, "MLX_SCORE")
        val ollamaScore = extractScore(judgeResponse, "OLLAMA_SCORE")
        val winner = extractWinner(judgeResponse)
        val reasoning = extractReasoning(judgeResponse)

        return JudgeResult(
            judgeModelName = config.judgeModelName,
            mlxQualityScore = mlxScore,
            ollamaQualityScore = ollamaScore,
            reasoning = reasoning,
            winner = winner
        )
    }

    @Action
    @AchievesGoal(
        description = "Compare results of the MLX and non-MLX benchmark tests including speed and quality.")
    fun compareResults(
        context: OperationContext,
        mlxTestResult: MlxTestResult,
        ollamaTestResult: OllamaTestResult,
        judgeResult: JudgeResult
    ): ComparisonResult {
        println("\n------------- Comparing Results -------------")

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

        println("SPEED:")
        println("Faster model: ${if (mlxFaster) "MLX" else "Ollama"}")
        println("Tokens per second difference: ${String.format("%.2f", tpsDifference)}%")
        println("Speed difference: ${String.format("%.2f", speedDifference)}%")

        println("\nQUALITY: (Using ${judgeResult.judgeModelName} as judge)")
        println("MLX quality score: ${judgeResult.mlxQualityScore}/100")
        println("Ollama quality score: ${judgeResult.ollamaQualityScore}/100")
        println("Quality winner: ${judgeResult.winner}")
        println("Judge reasoning: ${judgeResult.reasoning}")
        println("------------------------------------------------\n")

        return ComparisonResult(
            mlxResult = mlxResult,
            ollamaResult = ollamaResult,
            speedDifferencePercent = speedDifference,
            mlxFaster = mlxFaster,
            qualityJudgment = judgeResult
        )
    }

    private fun calculateTokensPerSecond(response: String, timeMs: Long): Double {
        val estimatedTokens = response.length / 4
        val timeSeconds = timeMs / 1000.0
        return if (timeSeconds > 0) estimatedTokens / timeSeconds else 0.0
    }

    private fun extractScore(response: String, scoreLabel: String): Int {
        val pattern = "$scoreLabel:\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(response)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 50
    }

    private fun extractWinner(response: String): String {
        val pattern = "WINNER:\\s*(\\w+)".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(response)
        return match?.groupValues?.get(1) ?: "Tie"
    }

    private fun extractReasoning(response: String): String {
        val pattern = "REASONING:\\s*(.+)".toRegex(RegexOption.IGNORE_CASE)
        val match = pattern.find(response)
        return match?.groupValues?.get(1)?.trim() ?: "No reasoning provided"
    }
}
