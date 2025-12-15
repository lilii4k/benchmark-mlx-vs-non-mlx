package com.incept5.benchmark.model

data class TestResult(
    val modelName: String,
    val modelType: ModelType,
    val prompt: String,
    val response: String,
    val executionTimeMs: Long,
    val tokensPerSecond: Double? = null,
    val totalTokens: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val iterationCount: Int = 1
)

data class MlxTestResult(
    val result: TestResult
)

data class OllamaTestResult(
    val result: TestResult
)

enum class ModelType {
    MLX_LM_STUDIO,
    OLLAMA_STANDARD
}

data class JudgeResult(
    val judgeModelName: String,
    val mlxQualityScore: Int,
    val ollamaQualityScore: Int,
    val reasoning: String,
    val winner: String
)

data class IterationData(
    val iterationNumber: Int,
    val mlxResponse: String,
    val mlxTimeMs: Long,
    val mlxTokensPerSecond: Double,
    val ollamaResponse: String,
    val ollamaTimeMs: Long,
    val ollamaTokensPerSecond: Double,
    val judgeResult: JudgeResult
)

data class ComparisonResult(
    val mlxResult: TestResult,
    val ollamaResult: TestResult,
    val speedDifferencePercent: Double,
    val mlxFaster: Boolean,
    val qualityJudgment: JudgeResult? = null
)

data class AggregatedBenchmarkResult(
    val prompt: String,
    val mlxModelName: String,
    val ollamaModelName: String,
    val judgeModelName: String,
    val totalIterations: Int,
    val iterations: List<IterationData>,
    val avgMlxTimeMs: Long,
    val avgOllamaTimeMs: Long,
    val avgMlxTokensPerSecond: Double,
    val avgOllamaTokensPerSecond: Double,
    val avgMlxQualityScore: Double,
    val avgOllamaQualityScore: Double,
    val mlxWins: Int,
    val ollamaWins: Int,
    val ties: Int
)

data class TestConfiguration(
    val prompt: String,
    val mlxModelName: String,
    val ollamaModelName: String,
    val judgeModelName: String,
    val iterations: Int = 1
)
