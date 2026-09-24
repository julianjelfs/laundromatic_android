package com.julianjelfs.laundromatic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.julianjelfs.laundromatic.AppUiState
import com.julianjelfs.laundromatic.ItemAction
import com.julianjelfs.laundromatic.LaundryItem
import com.julianjelfs.laundromatic.ui.components.LaundryItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: AppUiState,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onShowAddDialog: () -> Unit,
    onToggleAllPaused: () -> Unit,
    onItemAction: (LaundryItem, ItemAction) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (uiState.isRefreshing) "Refreshing..." else "Laundromatic")
                        uiState.userEmail?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        enabled = !uiState.isRefreshing,
                        onClick = onRefresh,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh items",
                        )
                    }
                    IconButton(
                        enabled = uiState.items.isNotEmpty() && !uiState.isUpdatingAllItems,
                        onClick = onToggleAllPaused,
                    ) {
                        Icon(
                            imageVector = if (uiState.allItemsPaused) {
                                Icons.Filled.PlayCircle
                            } else {
                                Icons.Filled.PauseCircle
                            },
                            contentDescription = if (uiState.allItemsPaused) {
                                "Resume all items"
                            } else {
                                "Pause all items"
                            },
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddDialog,
                elevation = FloatingActionButtonDefaults.elevation(),
                text = { Text("Add item") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add item") },
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            when {
                uiState.isLoadingItems && uiState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.items.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        error = uiState.screenError,
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        uiState.screenError?.let { error ->
                            item {
                                ErrorBanner(error = error)
                            }
                        }
                        items(
                            items = uiState.items,
                            key = { it.id },
                        ) { item ->
                            LaundryItemCard(
                                item = item,
                                isBusy = item.id in uiState.busyItemIds,
                                onActionClick = onItemAction,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    error: String?,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (error == null) "No laundry items yet." else "Unable to load items.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = error ?: "Tap Add item to create first wash reminder.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ErrorBanner(error: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
