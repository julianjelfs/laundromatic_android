package com.julianjelfs.laundromatic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as LaundromaticApplication).repository

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value, authError = null) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value, authError = null) }
    }

    fun signIn() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAuthenticating = true,
                    authError = null,
                )
            }
            runCatching {
                repository.signIn(state.email, state.password)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        authError = error.message ?: "Unable to sign in.",
                    )
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
    }

    fun refresh() {
        val userId = _uiState.value.userId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, screenError = null) }
            runCatching {
                repository.refreshItems(userId)
            }.onSuccess { items ->
                _uiState.update {
                    it.copy(
                        items = items,
                        isRefreshing = false,
                        isLoadingItems = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        isLoadingItems = false,
                        screenError = error.message ?: "Refresh failed.",
                    )
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update {
            it.copy(
                addItemForm = AddItemForm(),
                isAddDialogVisible = true,
            )
        }
    }

    fun hideAddDialog() {
        _uiState.update {
            it.copy(
                addItemForm = AddItemForm(),
                isAddDialogVisible = false,
            )
        }
    }

    fun updateAddItemName(value: String) {
        _uiState.update {
            it.copy(
                addItemForm = it.addItemForm.copy(name = value, error = null),
            )
        }
    }

    fun updateAddItemInterval(value: String) {
        _uiState.update {
            it.copy(
                addItemForm = it.addItemForm.copy(intervalInDays = value, error = null),
            )
        }
    }

    fun updateAddItemLastWashed(value: String) {
        _uiState.update {
            it.copy(
                addItemForm = it.addItemForm.copy(daysSinceLastWash = value, error = null),
            )
        }
    }

    fun submitAddItem() {
        val state = _uiState.value
        val userId = state.userId ?: return
        val interval = state.addItemForm.intervalInDays.toIntOrNull()
        val daysSinceLastWash = state.addItemForm.daysSinceLastWash.toIntOrNull()

        if (state.addItemForm.name.isBlank() || interval == null || daysSinceLastWash == null) {
            _uiState.update {
                it.copy(
                    addItemForm = it.addItemForm.copy(
                        error = "Enter item name, wash frequency, days since last wash.",
                    ),
                )
            }
            return
        }

        if (interval <= 0 || daysSinceLastWash < 0) {
            _uiState.update {
                it.copy(
                    addItemForm = it.addItemForm.copy(
                        error = "Wash frequency must be > 0. Days since last wash must be >= 0.",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingAddItem = true) }
            runCatching {
                repository.addItem(
                    userId = userId,
                    name = state.addItemForm.name,
                    intervalInDays = interval,
                    daysSinceLastWash = daysSinceLastWash,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        addItemForm = AddItemForm(),
                        isAddDialogVisible = false,
                        isSubmittingAddItem = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmittingAddItem = false,
                        addItemForm = it.addItemForm.copy(
                            error = error.message ?: "Unable to add item.",
                        ),
                    )
                }
            }
        }
    }

    fun confirmItemAction(item: LaundryItem, action: ItemAction) {
        _uiState.update { it.copy(pendingAction = PendingAction.Item(item, action)) }
    }

    fun dismissPendingAction() {
        _uiState.update { it.copy(pendingAction = null) }
    }

    fun runPendingAction() {
        val pendingAction = _uiState.value.pendingAction ?: return
        dismissPendingAction()

        when (pendingAction) {
            is PendingAction.Item -> runItemAction(pendingAction.item, pendingAction.action)
        }
    }

    fun toggleAllPaused() {
        val state = _uiState.value
        val userId = state.userId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingAllItems = true,
                    screenError = null,
                )
            }
            runCatching {
                repository.setAllPaused(
                    userId = userId,
                    paused = !state.allItemsPaused,
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(screenError = error.message ?: "Unable to update all items.")
                }
            }
            _uiState.update { it.copy(isUpdatingAllItems = false) }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            repository.observeAuthState().collectLatest { user ->
                _uiState.update {
                    it.copy(
                        userId = user?.uid,
                        userEmail = user?.email,
                        password = "",
                        authError = null,
                        isAuthenticating = false,
                        screenError = null,
                        items = if (user == null) emptyList() else it.items,
                        isLoadingItems = user != null,
                    )
                }

                if (user == null) {
                    _uiState.update {
                        it.copy(
                            items = emptyList(),
                            isLoadingItems = false,
                            isRefreshing = false,
                            isUpdatingAllItems = false,
                        )
                    }
                    return@collectLatest
                }

                repository.observeItems(user.uid)
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                isLoadingItems = false,
                                isRefreshing = false,
                                screenError = error.message ?: "Unable to load items.",
                            )
                        }
                    }
                    .collect { items ->
                        _uiState.update {
                            it.copy(
                                items = items,
                                isLoadingItems = false,
                                isRefreshing = false,
                                screenError = null,
                            )
                        }
                    }
            }
        }
    }

    private fun runItemAction(item: LaundryItem, action: ItemAction) {
        val userId = _uiState.value.userId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    busyItemIds = it.busyItemIds + item.id,
                    screenError = null,
                )
            }
            runCatching {
                when (action) {
                    ItemAction.Delete -> repository.deleteItem(userId, item.id)
                    ItemAction.Pause -> repository.pauseItem(userId, item.id)
                    ItemAction.Resume -> repository.resumeItem(userId, item.id)
                    ItemAction.Wash -> repository.washItem(userId, item.id)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        screenError = error.message ?: "Action failed.",
                    )
                }
            }
            _uiState.update {
                it.copy(busyItemIds = it.busyItemIds - item.id)
            }
        }
    }
}

data class AppUiState(
    val email: String = "",
    val password: String = "",
    val isAuthenticating: Boolean = false,
    val authError: String? = null,
    val userId: String? = null,
    val userEmail: String? = null,
    val items: List<LaundryItem> = emptyList(),
    val isLoadingItems: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUpdatingAllItems: Boolean = false,
    val busyItemIds: Set<String> = emptySet(),
    val isAddDialogVisible: Boolean = false,
    val isSubmittingAddItem: Boolean = false,
    val addItemForm: AddItemForm = AddItemForm(),
    val pendingAction: PendingAction? = null,
    val screenError: String? = null,
) {
    val isSignedIn: Boolean = userId != null
    val allItemsPaused: Boolean = items.isNotEmpty() && items.all(LaundryItem::isPaused)
}

data class AddItemForm(
    val name: String = "",
    val intervalInDays: String = "",
    val daysSinceLastWash: String = "",
    val error: String? = null,
)

enum class ItemAction {
    Delete,
    Pause,
    Resume,
    Wash,
}

sealed interface PendingAction {
    data class Item(val item: LaundryItem, val action: ItemAction) : PendingAction
}
