package com.pygeniusai

import android.app.Application
import com.pygeniusai.data.UserProgressRepository
import com.pygeniusai.python.PythonRuntime

class PyGeniusApplication : Application() {
    
    lateinit var pythonRuntime: PythonRuntime
    lateinit var userProgressRepository: UserProgressRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Python runtime (mock for now - Chaquopy integration pending)
        pythonRuntime = PythonRuntime.getInstance()
        pythonRuntime.initialize(this)
        
        userProgressRepository = UserProgressRepository(this)
    }
    
    companion object {
        fun getInstance(app: Application): PyGeniusApplication {
            return app as PyGeniusApplication
        }
    }
}
