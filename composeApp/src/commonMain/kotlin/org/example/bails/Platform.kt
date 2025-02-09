package org.example.bails

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform