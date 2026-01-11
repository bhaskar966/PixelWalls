package com.bhaskar.pixelwalls.domain.model

// PromptTemplate.kt
data class PromptTemplate(
    val id: String,
    val category: PromptCategory,
    val template: String,
    val fixedWords: List<String>,
    val variables: Map<String, PromptVariable>
)

data class PromptVariable(
    val key: String,
    val displayName: String,
    val options: List<PromptOption>,
    val defaultIndex: Int = 0
)

data class PromptOption(
    val value: String,
    val icon: String? = null
)

enum class PromptCategory {
    IMAGINARY, NATURE, ABSTRACT, ARCHITECTURAL, FANTASY
}

