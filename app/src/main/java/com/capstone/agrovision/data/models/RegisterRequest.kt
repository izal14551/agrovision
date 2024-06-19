package com.capstone.agrovision.data.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)