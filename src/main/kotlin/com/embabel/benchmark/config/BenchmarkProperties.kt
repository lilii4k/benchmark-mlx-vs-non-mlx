package com.embabel.benchmark.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "benchmark")
class BenchmarkProperties {

    var defaultPrompt: String = """
        Document:
        Title: Feasibility Assessment of the Northbridge Autonomous Delivery Network (NADN)
        Author: Department of Urban Mobility Systems
        Date: March 2024
1. Overview
The NADN program proposes deploying 1,250 autonomous delivery drones across the city of Northbridge by 2030. The expected operational window is 6,000 hours per year per drone, with an estimated payload capacity of 3.2 kg, though a competing vendor claims they can increase payload by 18% without increasing energy consumption.
2. Energy Consumption Data
Field tests in 2023 showed average power use of 410 Wh per flight-hour under nominal weather conditions. However, during winter trials, energy use increased by 22–27%, with an average of 513 Wh per flight-hour when winds exceeded 18 km/h.
A proposed “adaptive rotor system” (ARS) is predicted to reduce winter energy overhead by 10% of the excess above nominal, though critics argue the ARS tests used artificially favorable wind tunnel conditions.
3. Economic Projection
The city estimates the NADN will reduce last-mile delivery emissions by 11,800 metric tons CO₂ annually and save local businesses $47.3 million per year.
However, an independent audit uncovered a contradictory figure: the emissions reduction was computed assuming full adoption rates of 95%, although historical adoption rates for similar tech have averaged 62–74%.
Maintenance per drone is projected at $1,480 annually, but the audit found that this number excluded the ARS maintenance overhead, which adds an additional $190 ± $40 per drone.
4. Safety + Incident Log (Excerpt)
Between 2022 and 2023, there were 17 critical incidents, of which:
6 were due to software navigation errors
8 due to battery degradation issues
3 due to unexpected bird interference
Of note: Incident #14 included conflicting telemetry logs—GPS data recorded a stable hover while IMU data registered a 4.3 m/s lateral drift.
5. Policy Considerations
A draft ordinance requires that all NADN drones maintain a minimum 45 m standoff distance from residential buildings except during emergency relief operations. Emergency exemptions last a maximum of 72 hours, although a 2021 court ruling suggests that temporary exemptions can be extended if “critical logistics continuity is at risk.”
Stakeholder feedback shows 38% public approval, 41% neutral, and 21% opposed—though opposition jumps to 44% in districts with above-average noise complaints.

        Based on the document above, answer the following questions:
Q1:If the adaptive rotor system works as predicted, what would the new average winter energy consumption per flight-hour be, assuming winter overhead remains 27% above nominal conditions?
Q2:Using the corrected maintenance numbers (including ARS overhead), what is the total annual maintenance cost for the full fleet of 1,250 drones?
Q3:Identify two contradictions in the economic or environmental projections and explain why they matter for policy decisions.
Q4:Given the incident log, which subsystem most likely needs priority redesign, and why? Include reasoning that uses the severity and nature of the failures.
Q5:A severe snowstorm triggers emergency relief operations lasting 68 hours. According to the policy text and court ruling, can the city legally extend exemptions beyond 72 hours in this situation?
Q6:How would a 30% decline in public approval in high-noise districts impact the probability of ordinance passage, considering the economic benefits and safety record?
Q7:Does the vendor’s 18% payload increase claim seem credible or risky in context of the given energy consumption data and winter overhead effects?
Q8:Summarize the feasibility of NADN in exactly three sentences, each addressing: technical viability, economic justification, policy/safety concerns.(Be strict: no more than three sentences.)
    """.trimIndent()

//    If no models / iteration number is provided in config, it will default to these:
    var mlxModel: String = "qwen3-4b-instruct-2507-mlx"

    var ollamaModel: String = "hopephoto/Qwen3-4B-Instruct-2507_q8:latest"

    var judgeModel: String = "openai/gpt-oss-20b"

    var iterations: Int = 100
}


