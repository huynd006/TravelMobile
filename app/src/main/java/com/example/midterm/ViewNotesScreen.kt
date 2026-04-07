package com.example.midterm

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.midterm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNotesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    var isAdmin by remember { mutableStateOf(false) }
    var isCheckingRole by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf(listOf<Note>()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedNoteForDetail by remember { mutableStateOf<Note?>(null) }

    // Kiểm tra quyền từ Firestore
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid).get()
                .addOnSuccessListener { doc ->
                    isAdmin = doc.getString("role") == "admin"
                    isCheckingRole = false
                }
                .addOnFailureListener { isCheckingRole = false }
        } else {
            isCheckingRole = false
        }
    }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("notes")
            .get()
            .addOnSuccessListener { result ->
                notes = result.documents.mapNotNull { it.toObject(Note::class.java) }
                isLoading = false
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Danh sách điểm du lịch", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
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
            if (isLoading || isCheckingRole) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryPurple)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    items(notes) { note ->
                        NoteItemWithAuth(
                            note = note,
                            isAdmin = isAdmin,
                            onDetail = { selectedNoteForDetail = note },
                            onEdit = { navController.navigate(Screen.UpdateNote.createRoute(note.noteId)) },
                            onDelete = {
                                NoteRepository.deleteNote(note.noteId, {
                                    notes = notes.filter { it.noteId != note.noteId }
                                    Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                                }, {})
                            }
                        )
                    }
                }
            }
        }
    }

    selectedNoteForDetail?.let { note ->
        DetailDialog(note = note, onDismiss = { selectedNoteForDetail = null })
    }
}

@Composable
fun NoteItemWithAuth(note: Note, isAdmin: Boolean, onDetail: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isImage = note.fileUrl.lowercase().let { 
        it.contains(".jpg") || it.contains(".jpeg") || it.contains(".png") || it.contains(".webp") || it.contains(".gif")
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (note.fileUrl.isNotBlank() && isImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(note.fileUrl).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(80.dp).background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (note.fileUrl.isNotBlank()) Icons.Default.InsertDriveFile else Icons.Default.Description,
                            contentDescription = null,
                            tint = SecondaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = note.description, style = MaterialTheme.typography.bodySmall, color = SoftText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onDetail, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)) {
                    Text("Chi tiết", fontSize = 12.sp)
                }

                if (isAdmin) {
                    FilledTonalButton(onClick = onEdit, modifier = Modifier.weight(0.8f), shape = RoundedCornerShape(10.dp)) {
                        Text("Sửa", fontSize = 12.sp)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.background(Color(0xFFFFEBEE), RoundedCornerShape(10.dp)).size(40.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DetailDialog(note: Note, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isImage = note.fileUrl.lowercase().let { 
        it.contains(".jpg") || it.contains(".jpeg") || it.contains(".png") || it.contains(".webp") || it.contains(".gif")
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.92f).wrapContentHeight().clip(RoundedCornerShape(28.dp)), color = Color.White) {
            Column {
                if (note.fileUrl.isNotBlank()) {
                    if (isImage) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(note.fileUrl).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.InsertDriveFile, null, modifier = Modifier.size(48.dp), tint = SecondaryBlue)
                                Text(note.fileName, color = SecondaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(note.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(note.description, style = MaterialTheme.typography.bodyLarge, color = SoftText)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (note.fileUrl.isNotBlank()) {
                        Button(
                            onClick = { downloadFile(context, note.fileUrl, note.fileName) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryBlue)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tải về máy")
                        }
                    }
                    
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

fun downloadFile(context: Context, url: String, fileName: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Đang tải: $fileName")
            .setDescription("Đang tải file từ Cloudinary...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Bắt đầu tải xuống...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Lỗi tải xuống: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
