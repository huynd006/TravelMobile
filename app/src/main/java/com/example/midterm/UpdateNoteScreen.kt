package com.example.midterm

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.midterm.ui.theme.AccentPink
import com.example.midterm.ui.theme.BgBottom
import com.example.midterm.ui.theme.BgTop
import com.example.midterm.ui.theme.CardWhite
import com.example.midterm.ui.theme.PrimaryPurple
import com.example.midterm.ui.theme.SecondaryBlue
import com.example.midterm.ui.theme.SoftText
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNoteScreen(
    navController: NavHostController,
    noteId: String
) {
    val context = navController.context

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var oldFileUrl by remember { mutableStateOf("") }
    var oldFileName by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("Chưa chọn file") }
    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
        }
        selectedFileUri = uri
        if (uri != null) {
            selectedFileName = uri.lastPathSegment ?: "Đã chọn file"
        }
    }

    LaunchedEffect(noteId) {
        FirebaseFirestore.getInstance()
            .collection("notes")
            .document(noteId)
            .get()
            .addOnSuccessListener { doc ->
                val note = doc.toObject(Note::class.java)
                if (note != null) {
                    title = note.title
                    description = note.description
                    oldFileUrl = note.fileUrl
                    oldFileName = note.fileName
                    if (note.fileName.isNotBlank()) {
                        selectedFileName = note.fileName
                    }
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(context, "Không tải được note: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chỉnh sửa", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Cập nhật",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryPurple)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Đang tải dữ liệu...",
                        style = MaterialTheme.typography.titleMedium,
                        color = SoftText
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        listOf(PrimaryPurple, SecondaryBlue, AccentPink)
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(22.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Chỉnh sửa địa điểm",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "_________________________________________",
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            UpdateInputField(
                                value = title,
                                onValueChange = { title = it },
                                label = "Tiêu đề",
                                placeholder = "Nhập tiêu đề",
                                icon = Icons.Default.NoteAdd,
                                minLines = 1
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            UpdateInputField(
                                value = description,
                                onValueChange = { description = it },
                                label = "Nội dung",
                                placeholder = "Nhập mô tả",
                                icon = Icons.Default.Description,
                                minLines = 5
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            FilledTonalButton(
                                onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFFEDEBFF),
                                    contentColor = PrimaryPurple
                                )
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Chọn file/ảnh mới", fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF8FBFF)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "File hiện tại: $selectedFileName",
                                        modifier = Modifier.weight(1f),
                                        color = SoftText,
                                        fontWeight = FontWeight.Medium
                                    )

                                    IconButton(
                                        onClick = {
                                            selectedFileUri = null
                                            selectedFileName = "Chưa chọn file"
                                            oldFileUrl = ""
                                            oldFileName = ""
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Xóa file đã chọn",
                                            tint = AccentPink
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    if (title.isBlank()) {
                                        Toast.makeText(context, "Vui lòng nhập title", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    if (description.isBlank()) {
                                        Toast.makeText(context, "Vui lòng nhập description", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    isUpdating = true

                                    if (selectedFileUri != null) {
                                        NoteRepository.updateNoteWithNewFile(
                                            noteId = noteId,
                                            fileUri = selectedFileUri!!,
                                            title = title,
                                            description = description,
                                            onSuccess = {
                                                isUpdating = false
                                                Toast.makeText(context, "Cập nhật địa điểm thành công", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            },
                                            onFailure = { error ->
                                                isUpdating = false
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    } else {
                                        NoteRepository.updateNoteWithoutChangingFile(
                                            noteId = noteId,
                                            title = title,
                                            description = description,
                                            oldFileUrl = oldFileUrl,
                                            oldFileName = oldFileName,
                                            onSuccess = {
                                                isUpdating = false
                                                Toast.makeText(context, "Cập nhật địa điểm thành công", Toast.LENGTH_SHORT).show()
                                                navController.popBackStack()
                                            },
                                            onFailure = { error ->
                                                isUpdating = false
                                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                                enabled = !isUpdating
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    if (isUpdating) "Đang cập nhật..." else "Lưu cập nhật",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        singleLine = minLines == 1,
        minLines = minLines,
        label = { Text(label, fontWeight = FontWeight.SemiBold) },
        placeholder = { Text(placeholder, color = SoftText.copy(alpha = 0.7f)) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = PrimaryPurple)
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8F7FF),
            unfocusedContainerColor = Color(0xFFF8F7FF),
            disabledContainerColor = Color(0xFFF8F7FF),
            focusedIndicatorColor = PrimaryPurple,
            unfocusedIndicatorColor = BorderSoft,
            focusedLabelColor = PrimaryPurple,
            unfocusedLabelColor = SoftText,
            cursorColor = PrimaryPurple
        )
    )
}