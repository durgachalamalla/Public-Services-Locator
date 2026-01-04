package com.example.publicserviceslocator.features.citysearch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.publicserviceslocator.presentation.viewmodels.UserLocation

@Composable
fun CitySearchBar(
    onLocationResult: (UserLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchViewModel: CitySearchViewModel = viewModel()
    val query by searchViewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Helper function to trigger the search logic
    val executeSearch = {
        if (query.isNotBlank()) {
            searchViewModel.performCitySearch(context) { newLocation ->
                // This updates the location in ServiceListScreen
                onLocationResult(newLocation)
                focusManager.clearFocus() // Hide keyboard after search
            }
        }
    }

    OutlinedTextField(
        value = query,
        onValueChange = { searchViewModel.onQueryChanged(it) },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search city (e.g., Toronto, ON)") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.LocationCity,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { searchViewModel.onQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                    }
                }
                IconButton(onClick = executeSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { executeSearch() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.LightGray
        )
    )
}