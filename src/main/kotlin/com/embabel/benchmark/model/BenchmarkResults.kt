package com.embabel.benchmark.model

import java.time.Duration

data class TestResult(
    val modelName: String,
    val modelType: ModelType,
    val prompt: String,
    val response: String,
    val executionTimeMs: Long,
    val tokensPerSecond: Double? = null,
    val totalTokens: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
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

data class ComparisonResult(
    val mlxResult: TestResult,
    val ollamaResult: TestResult,
    val speedDifferencePercent: Double,
    val mlxFaster: Boolean,
)

data class TestConfiguration(
    val prompts: List<String>,
    val mlxModelName: String,
    val ollamaModelName: String,
    val iterations: Int = 1
)
