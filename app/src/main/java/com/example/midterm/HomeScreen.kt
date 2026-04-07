package com.example.midterm

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

private val BgTop = Color(0xFFF6F2FF)
private val BgBottom = Color(0xFFEAF4FF)
private val PrimaryPurple = Color(0xFF6C63FF)
private val SecondaryBlue = Color(0xFF4A90E2)
private val AccentPink = Color(0xFFFF6FAE)
private val CardWhite = Color(0xFFFCFCFF)
private val SoftText = Color(0xFF5A5F73)
val BorderSoft = Color(0xFFE6E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isAdmin = currentUser?.email == "duchuy@gmail.com"

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("Chưa chọn file") }
    var isSaving by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.lastPathSegment ?: "Đã chọn file"
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Travel Note", fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = if (isAdmin) "Chế độ Quản trị viên" else "Chế độ Người xem",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Signin.route) { popUpTo(0) }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeaderHeroCard(isAdmin)

                Spacer(modifier = Modifier.height(20.dp))

                if (isAdmin) {
                    AdminCreateNoteSection(
                        title = title,
                        onTitleChange = { title = it },
                        description = description,
                        onDescriptionChange = { description = it },
                        selectedFileName = selectedFileName,
                        isSaving = isSaving,
                        onPickFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                        onClearFile = {
                            selectedFileUri = null
                            selectedFileName = "Chưa chọn file"
                        },
                        onSave = {
                            if (title.isBlank() || description.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                                return@AdminCreateNoteSection
                            }
                            isSaving = true
                            val callback = {
                                isSaving = false
                                title = ""; description = ""; selectedFileUri = null; selectedFileName = "Chưa chọn file"
                                Toast.makeText(context, "Thành công!", Toast.LENGTH_SHORT).show()
                            }
                            if (selectedFileUri != null) {
                                NoteRepository.uploadFileAndCreateNewNote(selectedFileUri!!, title, description, callback, { isSaving = false })
                            } else {
                                NoteRepository.createNewNoteWithoutFile(title, description, callback, { isSaving = false })
                            }
                        }
                    )
                } else {
                    UserWelcomeSection()
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { navController.navigate(Screen.ViewNotes.route) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("XEM TẤT CẢ ĐỊA ĐIỂM", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HeaderHeroCard(isAdmin: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(PrimaryPurple, SecondaryBlue, AccentPink)), RoundedCornerShape(28.dp))
                .padding(vertical = 40.dp, horizontal = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isAdmin) "Xin chào Admin!" else "Chào mừng bạn!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAdmin) "________________________________________"
                           else "Bạn chỉ có thể xem các địa điểm du lịch.",
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AdminCreateNoteSection(
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    selectedFileName: String, isSaving: Boolean,
    onPickFile: () -> Unit, onClearFile: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Thêm địa điểm mới", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = title, onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(), label = { Text("Tiêu đề") },
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description, onValueChange = onDescriptionChange,
                modifier = Modifier.fillMaxWidth(), label = { Text("Nội dung") },
                shape = RoundedCornerShape(16.dp), minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onPickFile, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Text("Đính kèm")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedFileName, maxLines = 1, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                if (selectedFileName != "Chưa chọn file") {
                    IconButton(onClick = onClearFile) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSave, enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(if (isSaving) "ĐANG LƯU..." else "LƯU ĐỊA ĐIỂM", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun UserWelcomeSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(60.dp), tint = PrimaryPurple)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Không gian đọc", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Bạn đang đăng nhập với quyền Người xem. Hãy nhấn nút bên dưới để khám phá các địa điểm nổi bật.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = SoftText
            )
        }
    }
}
