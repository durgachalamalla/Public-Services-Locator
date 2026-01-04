package com.example.publicserviceslocator.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.publicserviceslocator.data.remote.ServiceResult
import com.example.publicserviceslocator.features.citysearch.CitySearchBar
import com.example.publicserviceslocator.navigation.Screen
import com.example.publicserviceslocator.presentation.viewmodels.ServiceListViewModel
import com.example.publicserviceslocator.presentation.viewmodels.UserLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    navController: NavController,
    userLocation: UserLocation,
    viewModel: ServiceListViewModel
) {
    // UI Logic States
    var isMapView by remember { mutableStateOf(false) }
    var activeLocation by remember { mutableStateOf(userLocation) }

    // Bottom Sheet States
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedService by remember { mutableStateOf<ServiceResult?>(null) }

    // ViewModel Observation
    val services by viewModel.services.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val maxDistance by viewModel.maxDistance.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()

    // Trigger search when location updates
    LaunchedEffect(activeLocation) {
        viewModel.loadServices(activeLocation)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nearby Services", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { isMapView = !isMapView }) {
                        Icon(if (isMapView) Icons.Default.List else Icons.Default.Map, "Toggle View")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Use a Box to allow the BottomSheet to overlay correctly
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {

                // --- LOCATION SEARCH & RESET ---
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        CitySearchBar(onLocationResult = { activeLocation = it })
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { activeLocation = userLocation },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, "Reset to GPS")
                    }
                }

                // --- FILTER CONTROLS ---
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Search by name...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Slider(
                    value = maxDistance.toFloat(),
                    onValueChange = { viewModel.onDistanceChanged(it.toDouble()) },
                    valueRange = 1f..20f
                )
                Text(
                    text = "Radius: ${maxDistance.toInt()} km | Found: ${services.size}",
                    style = MaterialTheme.typography.labelSmall
                )

                if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 4.dp))

                // --- CONTENT SWITCHER (MAP vs LIST) ---
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isMapView) {
                        val cameraPositionState = rememberCameraPositionState {
                            position = CameraPosition.fromLatLngZoom(
                                LatLng(activeLocation.latitude, activeLocation.longitude),
                                13f
                            )
                        }

                        // Sync map camera when location changes
                        LaunchedEffect(activeLocation) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                LatLng(activeLocation.latitude, activeLocation.longitude),
                                13f
                            )
                        }

                        GoogleMap(
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            cameraPositionState = cameraPositionState
                        ) {
                            services.forEach { uiItem ->
                                val pos = LatLng(
                                    uiItem.service.geometry?.location?.lat ?: 0.0,
                                    uiItem.service.geometry?.location?.lng ?: 0.0
                                )
                                Marker(
                                    state = MarkerState(position = pos),
                                    title = uiItem.service.name,
                                    onClick = {
                                        selectedService = uiItem.service
                                        showBottomSheet = true
                                        true // Consume click to show our custom sheet
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(services) { uiItem ->
                                ServiceItem(
                                    service = uiItem.service,
                                    distanceKm = uiItem.distanceKm,
                                    isFavorite = favoriteIds.contains(uiItem.service.place_id),
                                    onFavoriteClick = { viewModel.toggleFavorite(uiItem.service) },
                                    onClick = {
                                        selectedService = uiItem.service
                                        showBottomSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- DETAILS BOTTOM SHEET ---
            if (showBottomSheet && selectedService != null) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    ServiceDetailsSheetContent(
                        service = selectedService!!,
                        onViewDetailsFull = {
                            showBottomSheet = false
                            val id = selectedService?.place_id ?: ""
                            navController.navigate(Screen.Details.createRoute(id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceDetailsSheetContent(
    service: ServiceResult,
    onViewDetailsFull: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            text = service.name ?: "Unknown Service",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = service.vicinity ?: "No address available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onViewDetailsFull,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("View Full Details & Reviews")
        }
    }
}

// Keep your existing ServiceItem, IconContainer, and CategoryTag functions below...
@Composable
fun ServiceItem(
    service: ServiceResult,
    distanceKm: Double,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconContainer(service.types?.firstOrNull())

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name ?: "Unknown Service",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = service.vicinity ?: "No address available",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                CategoryTag(service.types?.firstOrNull() ?: "Service")
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
                Text(
                    text = "${"%.1f".format(distanceKm)} km",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun IconContainer(type: String?) {
    val (icon, tintColor, containerColor) = when (type?.lowercase()) {
        "hospital", "health" -> Triple(Icons.Filled.LocalHospital, Color(0xFFE50914), Color(0xFFFFE5E5))
        "school", "university" -> Triple(Icons.Filled.School, Color(0xFF3F51B5), Color(0xFFE8EAF6))
        "library" -> Triple(Icons.Filled.MenuBook, Color(0xFF00BFFF), Color(0xFFE0F7FA))
        "police", "law" -> Triple(Icons.Filled.Security, Color(0xFFF9A825), Color(0xFFFFF8E1))
        "park" -> Triple(Icons.Filled.Park, Color(0xFF4CAF50), Color(0xFFE8F5E9))
        else -> Triple(Icons.Filled.Place, Color(0xFF607D8B), Color(0xFFECEFF1))
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
fun CategoryTag(category: String) {
    val displayText = category.replace('_', ' ').split(" ").joinToString(" ") {
        it.replaceFirstChar { char -> char.uppercase() }
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFF0F0F0),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color.DarkGray
        )
    }
}