package com.example.gloomhavendeck

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

class GloomhavenApp : Application() {
    companion object {
        lateinit var instance: GloomhavenApp
    }

    init {
        instance = this
    }
}