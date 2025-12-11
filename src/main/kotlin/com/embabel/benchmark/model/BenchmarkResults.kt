package com.embabel.benchmark.model

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

data class ComparisonResult(
    val mlxResult: TestResult,
    val ollamaResult: TestResult,
    val speedDifferencePercent: Double,
    val mlxFaster: Boolean,
    val qualityJudgment: JudgeResult? = null
)

data class TestConfiguration(
    val prompt: String,
    val mlxModelName: String,
    val ollamaModelName: String,
    val judgeModelName: String,
    val iterations: Int = 1
)
