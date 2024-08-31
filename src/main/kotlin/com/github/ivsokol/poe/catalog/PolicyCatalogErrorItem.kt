package com.github.ivsokol.poe.catalog

data class PolicyCatalogErrorItem(
    val id: String,
    val errorType: ErrorTypeEnum,
    val message: String
)
