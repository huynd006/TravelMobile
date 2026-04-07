package com.example.midterm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.midterm.ui.theme.MIDTERMTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initCloudinary()

        setContent {
            MIDTERMTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MyNavigation()
                }
            }
        }
    }

    private fun initCloudinary() {
        val config = hashMapOf(
            "cloud_name" to "dxhvrft17",
            "api_key"    to "477268979769662",
            "api_secret" to "vIor8V0MxPcknTgFsKEBRdRI4K0"
        )

        try {
            MediaManager.init(applicationContext, config)
            Log.d("CloudinaryInit", "Init Cloudinary thành công")
        } catch (e: IllegalStateException) {
            Log.e("CloudinaryInit", "MediaManager đã được khởi tạo trước đó", e)
        } catch (e: Exception) {
            Log.e("CloudinaryInit", "Lỗi khởi tạo Cloudinary", e)
        }
    }
}

@Composable
fun MyNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Signin.route
    ) {
        composable(Screen.Signin.route) {
            SignIn(navController = navController)
        }

        composable(Screen.Signup.route) {
            SignUp(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.ViewNotes.route) {
            ViewNotesScreen(navController = navController)
        }

        composable(Screen.UpdateNote.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
            UpdateNoteScreen(
                navController = navController,
                noteId = noteId
            )
        }
    }
}
