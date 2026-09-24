package com.julianjelfs.laundromatic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.julianjelfs.laundromatic.AppUiState
import com.julianjelfs.laundromatic.AppViewModel
import com.julianjelfs.laundromatic.ItemAction
import com.julianjelfs.laundromatic.LaundryItem
import com.julianjelfs.laundromatic.PendingAction
import com.julianjelfs.laundromatic.ui.components.AddItemDialog
import com.julianjelfs.laundromatic.ui.components.ConfirmItemActionDialog
import com.julianjelfs.laundromatic.ui.screens.HomeScreen
import com.julianjelfs.laundromatic.ui.screens.LoginScreen
import com.julianjelfs.laundromatic.ui.theme.LaundromaticTheme

@Composable
fun LaundromaticApp(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    LaundromaticApp(
        modifier = modifier,
        uiState = uiState,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onSignIn = viewModel::signIn,
        onSignOut = viewModel::signOut,
        onRefresh = viewModel::refresh,
        onShowAddDialog = viewModel::showAddDialog,
        onDismissAddDialog = viewModel::hideAddDialog,
        onAddItemNameChange = viewModel::updateAddItemName,
        onAddItemIntervalChange = viewModel::updateAddItemInterval,
        onAddItemLastWashedChange = viewModel::updateAddItemLastWashed,
        onSubmitAddItem = viewModel::submitAddItem,
        onItemAction = viewModel::confirmItemAction,
        onDismissPendingAction = viewModel::dismissPendingAction,
        onConfirmPendingAction = viewModel::runPendingAction,
        onToggleAllPaused = viewModel::toggleAllPaused,
    )
}

@Composable
fun LaundromaticApp(
    modifier: Modifier = Modifier,
    uiState: AppUiState,
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onShowAddDialog: () -> Unit = {},
    onDismissAddDialog: () -> Unit = {},
    onAddItemNameChange: (String) -> Unit = {},
    onAddItemIntervalChange: (String) -> Unit = {},
    onAddItemLastWashedChange: (String) -> Unit = {},
    onSubmitAddItem: () -> Unit = {},
    onItemAction: (LaundryItem, ItemAction) -> Unit = { _, _ -> },
    onDismissPendingAction: () -> Unit = {},
    onConfirmPendingAction: () -> Unit = {},
    onToggleAllPaused: () -> Unit = {},
) {
    if (uiState.isSignedIn) {
        HomeScreen(
            modifier = modifier,
            uiState = uiState,
            onRefresh = onRefresh,
            onSignOut = onSignOut,
            onShowAddDialog = onShowAddDialog,
            onToggleAllPaused = onToggleAllPaused,
            onItemAction = onItemAction,
        )
    } else {
        LoginScreen(
            modifier = modifier,
            uiState = uiState,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onSignIn = onSignIn,
        )
    }

    if (uiState.isAddDialogVisible) {
        AddItemDialog(
            form = uiState.addItemForm,
            isSubmitting = uiState.isSubmittingAddItem,
            onDismiss = onDismissAddDialog,
            onNameChange = onAddItemNameChange,
            onIntervalChange = onAddItemIntervalChange,
            onLastWashedChange = onAddItemLastWashedChange,
            onSubmit = onSubmitAddItem,
        )
    }

    val pendingAction = uiState.pendingAction
    if (pendingAction is PendingAction.Item) {
        ConfirmItemActionDialog(
            item = pendingAction.item,
            action = pendingAction.action,
            onDismiss = onDismissPendingAction,
            onConfirm = onConfirmPendingAction,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LaundromaticAppPreview() {
    LaundromaticTheme {
        LaundromaticApp(
            uiState = AppUiState(
                userId = "preview",
                userEmail = "preview@example.com",
                items = listOf(
                    LaundryItem(
                        id = "1",
                        name = "Bath towels",
                        intervalInDays = 7,
                        lastWashed = System.currentTimeMillis(),
                        pausedAt = null,
                    ),
                ),
            ),
        )
    }
}
