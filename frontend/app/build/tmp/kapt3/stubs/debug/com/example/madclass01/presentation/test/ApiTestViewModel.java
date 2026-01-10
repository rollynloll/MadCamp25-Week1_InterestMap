package com.example.madclass01.presentation.test;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\rJ\u0006\u0010\u000f\u001a\u00020\rR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0010"}, d2 = {"Lcom/example/madclass01/presentation/test/ApiTestViewModel;", "Landroidx/lifecycle/ViewModel;", "backendRepository", "Lcom/example/madclass01/data/repository/BackendRepository;", "(Lcom/example/madclass01/data/repository/BackendRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/madclass01/presentation/test/ApiTestUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "testCreateUser", "", "testGetUser", "testHealthCheck", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ApiTestViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.data.repository.BackendRepository backendRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.madclass01.presentation.test.ApiTestUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.test.ApiTestUiState> uiState = null;
    
    @javax.inject.Inject()
    public ApiTestViewModel(@org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.repository.BackendRepository backendRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.test.ApiTestUiState> getUiState() {
        return null;
    }
    
    public final void testHealthCheck() {
    }
    
    public final void testCreateUser() {
    }
    
    public final void testGetUser() {
    }
}