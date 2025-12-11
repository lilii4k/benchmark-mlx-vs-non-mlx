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
        println("\n========== Preparing LLM Benchmark ==========")
        println("MLX Model (LM Studio): ${properties.mlxModel}")
        println("Non-MLX Model (Ollama): ${properties.ollamaModel}")
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
    @AchievesGoal(
        description = "Run benchmark comparing MLX and Ollama models across all iterations with quality judgments.")
    fun runCompleteBenchmark(context: OperationContext, config: TestConfiguration): AggregatedBenchmarkResult {

        val iterationDataList = mutableListOf<IterationData>()

        for (i in 1..config.iterations) {
            println("\n--------- Iteration $i/${config.iterations} ---------\n")

            // Test MLX model
            println("Testing MLX model: '${config.mlxModelName}'... \n")
            var mlxResponse = ""
            val mlxTime = measureTimeMillis {
                mlxResponse = context.ai()
                    .withLlm(LlmOptions.withModel(config.mlxModelName).withTemperature(0.2))
                    .generateText(config.prompt)
            }
            val mlxTps = calculateTokensPerSecond(mlxResponse, mlxTime)
            println("Finished (${mlxTime}ms, ${String.format("%.2f", mlxTps)} tokens/sec).")

            // Test Ollama model
            println("\nTesting Ollama model: '${config.ollamaModelName}'... \n")
            var ollamaResponse = ""
            val ollamaTime = measureTimeMillis {
                ollamaResponse = context.ai()
                    .withLlm(LlmOptions.withModel(config.ollamaModelName).withTemperature(0.2))
                    .generateText(config.prompt)
            }
            val ollamaTps = calculateTokensPerSecond(ollamaResponse, ollamaTime)
            println("Finished (${ollamaTime}ms, ${String.format("%.2f", ollamaTps)} tokens/sec).")

            // Judge this pair immediately
            println("\nComparing quality of responses...\n")
            val judgeResult = judgeResponsePair(
                context = context,
                judgeModelName = config.judgeModelName,
                originalPrompt = config.prompt,
                mlxResponse = mlxResponse,
                ollamaResponse = ollamaResponse
            )
            println("\nJudgement Complete - Winner: ${judgeResult.winner}")

            // Store iteration data
            iterationDataList.add(
                IterationData(
                    iterationNumber = i,
                    mlxResponse = mlxResponse,
                    mlxTimeMs = mlxTime,
                    mlxTokensPerSecond = mlxTps,
                    ollamaResponse = ollamaResponse,
                    ollamaTimeMs = ollamaTime,
                    ollamaTokensPerSecond = ollamaTps,
                    judgeResult = judgeResult
                )
            )
        }

        // Calculate aggregated statistics
        val avgMlxTime = iterationDataList.map { it.mlxTimeMs }.average().toLong()
        val avgOllamaTime = iterationDataList.map { it.ollamaTimeMs }.average().toLong()
        val avgMlxTps = iterationDataList.map { it.mlxTokensPerSecond }.average()
        val avgOllamaTps = iterationDataList.map { it.ollamaTokensPerSecond }.average()
        val avgMlxScore = iterationDataList.map { it.judgeResult.mlxQualityScore }.average()
        val avgOllamaScore = iterationDataList.map { it.judgeResult.ollamaQualityScore }.average()

        val mlxWins = iterationDataList.count { it.judgeResult.winner.equals("MLX", ignoreCase = true) }
        val ollamaWins = iterationDataList.count { it.judgeResult.winner.equals("Ollama", ignoreCase = true) }
        val ties = iterationDataList.count { it.judgeResult.winner.equals("Tie", ignoreCase = true) }

        val result = AggregatedBenchmarkResult(
            prompt = config.prompt,
            mlxModelName = config.mlxModelName,
            ollamaModelName = config.ollamaModelName,
            judgeModelName = config.judgeModelName,
            totalIterations = config.iterations,
            iterations = iterationDataList,
            avgMlxTimeMs = avgMlxTime,
            avgOllamaTimeMs = avgOllamaTime,
            avgMlxTokensPerSecond = avgMlxTps,
            avgOllamaTokensPerSecond = avgOllamaTps,
            avgMlxQualityScore = avgMlxScore,
            avgOllamaQualityScore = avgOllamaScore,
            mlxWins = mlxWins,
            ollamaWins = ollamaWins,
            ties = ties
        )

        printFinalSummary(result)
        return result
    }

    private fun judgeResponsePair(
        context: OperationContext,
        judgeModelName: String,
        originalPrompt: String,
        mlxResponse: String,
        ollamaResponse: String
    ): JudgeResult {
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
            .withLlm(LlmOptions.withModel(judgeModelName).withTemperature(0.3))
            .generateText(judgePrompt)

        val mlxScore = extractScore(judgeResponse, "MLX_SCORE")
        val ollamaScore = extractScore(judgeResponse, "OLLAMA_SCORE")
        val winner = extractWinner(judgeResponse)
        val reasoning = extractReasoning(judgeResponse)

        return JudgeResult(
            judgeModelName = judgeModelName,
            mlxQualityScore = mlxScore,
            ollamaQualityScore = ollamaScore,
            reasoning = reasoning,
            winner = winner
        )
    }

    private fun printFinalSummary(result: AggregatedBenchmarkResult) {
        println("\n")
        println("=".repeat(70))
        println("FINAL BENCHMARK SUMMARY")
        println("=".repeat(70))
        println()
        println("[${result.mlxModelName}] (MLX) vs. [${result.ollamaModelName}] (non-MLX)")
        println()

        val speedDiff = if (result.avgMlxTimeMs < result.avgOllamaTimeMs) {
            ((result.avgOllamaTimeMs - result.avgMlxTimeMs).toDouble() / result.avgOllamaTimeMs) * 100
        } else {
            ((result.avgMlxTimeMs - result.avgOllamaTimeMs).toDouble() / result.avgMlxTimeMs) * 100
        }
        val fasterModel = if (result.avgMlxTimeMs < result.avgOllamaTimeMs) "MLX" else "Ollama"
        println("SPEED METRICS:")
        println("  Faster Model: $fasterModel (${String.format("%.2f", speedDiff)}% faster)")
        println("  Average Time (MLX): ${result.avgMlxTimeMs}ms")
        println("  Average Time (non-MLX): ${result.avgOllamaTimeMs}ms")
        println("  Average TPS (MLX): ${String.format("%.2f", result.avgMlxTokensPerSecond)} tokens/sec")
        println("  Average TPS (non-MLX): ${String.format("%.2f", result.avgOllamaTokensPerSecond)} tokens/sec")
        println()

        val overallWinner = when {
            result.mlxWins > result.ollamaWins -> "MLX"
            result.ollamaWins > result.mlxWins -> "Ollama"
            else -> "Tie"
        }
        println("QUALITY METRICS:")
        println("  Best Quality: $overallWinner")
        println("  Average Score (MLX): ${String.format("%.2f", result.avgMlxQualityScore)}/100")
        println("  Average Score (non-MLX): ${String.format("%.2f", result.avgOllamaQualityScore)}/100")
        println("  Wins (MLX): ${result.mlxWins}")
        println("  Wins (non-MLX): ${result.ollamaWins}")
        println("  Ties: ${result.ties}")
        println()
        println("=".repeat(70))
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
