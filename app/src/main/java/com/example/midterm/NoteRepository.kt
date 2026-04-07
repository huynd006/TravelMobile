package com.example.midterm

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.UUID

object NoteRepository {

    private const val TAG = "NoteRepository"

    fun uploadFileAndCreateNewNote(
        fileUri: Uri,
        title: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val noteId = UUID.randomUUID().toString()

        // Sử dụng Signed Upload (không dùng .unsigned())
        MediaManager.get()
            .upload(fileUri)
            .option("folder", "notes")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Cloudinary upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.d(TAG, "Cloudinary upload progress: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    Log.d(TAG, "Cloudinary upload success: $resultData")

                    val fileUrl = resultData["secure_url"]?.toString().orEmpty()
                    val originalName = resultData["original_filename"]?.toString()
                        ?: fileUri.lastPathSegment
                        ?: "file"

                    val note: Map<String, Any> = mapOf(
                        "noteId" to noteId,
                        "title" to title,
                        "description" to description,
                        "fileUrl" to fileUrl,
                        "fileName" to originalName,
                        "createdAt" to System.currentTimeMillis()
                    )

                    FirebaseFirestore.getInstance()
                        .collection("notes")
                        .document(noteId)
                        .set(note)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            onFailure("Lưu Firestore thất bại: ${e.message}")
                        }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Cloudinary upload error: ${error.description}")
                    onFailure("Upload Cloudinary thất bại: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Cloudinary upload reschedule: ${error.description}")
                }
            })
            .dispatch()
    }

    fun updateNoteWithNewFile(
        noteId: String,
        fileUri: Uri,
        title: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Sử dụng Signed Upload
        MediaManager.get()
            .upload(fileUri)
            .option("folder", "notes")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Cloudinary update upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.d(TAG, "Cloudinary update progress: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val fileUrl = resultData["secure_url"]?.toString().orEmpty()
                    val originalName = resultData["original_filename"]?.toString()
                        ?: fileUri.lastPathSegment
                        ?: "file"

                    updateNoteInFirestore(
                        noteId = noteId,
                        title = title,
                        description = description,
                        fileUrl = fileUrl,
                        fileName = originalName,
                        onSuccess = onSuccess,
                        onFailure = onFailure
                    )
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onFailure("Upload Cloudinary thất bại: ${error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    onFailure("Upload bị hoãn: ${error.description}")
                }
            })
            .dispatch()
    }

    // Các hàm khác giữ nguyên...
    fun createNewNoteWithoutFile(title: String, description: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val noteId = UUID.randomUUID().toString()
        val note = mapOf("noteId" to noteId, "title" to title, "description" to description, "fileUrl" to "", "fileName" to "", "createdAt" to System.currentTimeMillis())
        FirebaseFirestore.getInstance().collection("notes").document(noteId).set(note).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it.message ?: "") }
    }

    fun deleteNote(noteId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection("notes").document(noteId).delete().addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it.message ?: "") }
    }

    fun updateNoteWithoutChangingFile(noteId: String, title: String, description: String, oldFileUrl: String, oldFileName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        updateNoteInFirestore(noteId, title, description, oldFileUrl, oldFileName, onSuccess, onFailure)
    }

    private fun updateNoteInFirestore(noteId: String, title: String, description: String, fileUrl: String, fileName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val updatedNote = mapOf("noteId" to noteId, "title" to title, "description" to description, "fileUrl" to fileUrl, "fileName" to fileName, "updatedAt" to System.currentTimeMillis())
        FirebaseFirestore.getInstance().collection("notes").document(noteId).set(updatedNote, SetOptions.merge()).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it.message ?: "") }
    }
}
