package com.example.midterm

sealed class Screen(val route: String) {
    object Signin : Screen("signin")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object ViewNotes : Screen("view_notes")
    object UpdateNote : Screen("update_note/{noteId}") {
        fun createRoute(noteId: String) = "update_note/$noteId"
    }
}