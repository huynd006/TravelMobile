package com.example.midterm

import android.net.Uri
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object UpdateNoteRepository {

    fun updateNoteWithNewFile(
        noteId: String,
        fileUri: Uri,
        title: String,
        content: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val fileNameInStorage = "notes/${UUID.randomUUID()}"
        val storageRef = FirebaseStorage.getInstance().reference.child(fileNameInStorage)

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        updateNoteInDatabases(
                            noteId = noteId,
                            title = title,
                            content = content,
                            description = description,
                            fileUrl = downloadUri.toString(),
                            fileName = fileUri.lastPathSegment ?: "file",
                            onSuccess = onSuccess,
                            onFailure = onFailure
                        )
                    }
                    .addOnFailureListener { e ->
                        onFailure("Không lấy được link file mới: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onFailure("Upload file mới thất bại: ${e.message}")
            }
    }

    fun updateNoteWithoutChangingFile(
        noteId: String,
        title: String,
        content: String,
        description: String,
        oldFileUrl: String,
        oldFileName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        updateNoteInDatabases(
            noteId = noteId,
            title = title,
            content = content,
            description = description,
            fileUrl = oldFileUrl,
            fileName = oldFileName,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun updateNoteInDatabases(
        noteId: String,
        title: String,
        content: String,
        description: String,
        fileUrl: String,
        fileName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val updatedNote: Map<String, Any> = mapOf(
            "noteId" to noteId,
            "title" to title,
            "content" to content,
            "description" to description,
            "fileUrl" to fileUrl,
            "fileName" to fileName,
            "updatedAt" to System.currentTimeMillis()
        )

        val firestore = FirebaseFirestore.getInstance()
        val realtimeDb = FirebaseDatabase.getInstance().reference

        firestore.collection("notes")
            .document(noteId)
            .update(updatedNote)
            .addOnSuccessListener {
                realtimeDb.child("notes")
                    .child(noteId)
                    .updateChildren(updatedNote)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure("Cập nhật Realtime Database thất bại: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onFailure("Cập nhật Firestore thất bại: ${e.message}")
            }
    }
}