package com.incept5.benchmark

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application for the LLM Benchmark Agent
 *
 * This application uses the Embabel Agent Framework to create an autonomous
 * benchmarking agent that compares MLX-optimized LLMs (via LM Studio) against
 * standard Ollama models.
 *
 * Usage:
 *   mvn spring-boot:run
 *
 * Or with custom prompt:
 *   mvn spring-boot:run -Dspring-boot.run.arguments="--testPrompt='Your custom prompt here'"
 */
@SpringBootApplication
@ConfigurationPropertiesScan
class BenchmarkApplication

fun main(args: Array<String>) {
    runApplication<BenchmarkApplication>(*args)
}
