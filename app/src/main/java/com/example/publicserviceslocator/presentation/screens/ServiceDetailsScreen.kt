package com.example.publicserviceslocator.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.publicserviceslocator.presentation.viewmodels.ServiceDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    navController: NavController,
    serviceId: String,
    viewModel: ServiceDetailsViewModel
) {
    // Collecting StateFlows from ViewModel
    val details by viewModel.details.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFav by viewModel.isFavorite.collectAsState()
    val context = LocalContext.current

    // Trigger data fetching and favorite status check on entry
    LaunchedEffect(serviceId) {
        if (serviceId.isNotEmpty()) {
            viewModel.fetchDetails(serviceId)
            // We check the favorite status immediately using the ID passed from navigation
            viewModel.checkIfFavorite(serviceId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Only show/enable the heart button if data is loaded
                    details?.let { serviceData ->
                        IconButton(onClick = {
                            viewModel.toggleFavorite(serviceData)
                        }) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFav) Color.Red else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                details != null -> {
                    val data = details!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Section
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = data.name ?: "Unknown Service",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = data.vicinity ?: "No address available",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info Section
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            DetailRow(
                                icon = Icons.Default.Star,
                                label = "Rating",
                                value = data.rating?.toString() ?: "No rating available"
                            )
                            DetailRow(
                                icon = Icons.Default.Phone,
                                label = "Phone",
                                value = data.formatted_phone_number ?: "Not listed"
                            )

                            val isOpen = if (data.opening_hours?.open_now == true) "Open Now" else "Closed"
                            DetailRow(
                                icon = Icons.Default.AccessTime,
                                label = "Status",
                                value = isOpen
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Buttons (Call & Website)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${data.formatted_phone_number}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !data.formatted_phone_number.isNullOrEmpty(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Call")
                            }



                            OutlinedButton(
                                onClick = {
                                    // Updated logic starts here
                                    val webUrl = data.website
                                    if (!webUrl.isNullOrEmpty()) {
                                        val formattedUrl = if (!webUrl.startsWith("http://") && !webUrl.startsWith("https://")) {
                                            "https://$webUrl"
                                        } else {
                                            webUrl
                                        }
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !data.website.isNullOrEmpty(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Website")
                            }

                        }
                    }
                }
                else -> {
                    Text(
                        text = "Unable to load service details.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}