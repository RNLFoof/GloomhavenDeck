package com.example.gloomhavendeck

import android.app.Application

class GloomhavenApp : Application() {
    companion object {
        lateinit var instance: GloomhavenApp
    }

    init {
        instance = this
    }
}