package com.example.madclass01.presentation.login.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\"\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00152\b\u0010\u0017\u001a\u0004\u0018\u00010\u0015J\u0006\u0010\u0018\u001a\u00020\u0013J\u0006\u0010\u0019\u001a\u00020\u0013J\u000e\u0010\u001a\u001a\u00020\u00132\u0006\u0010\u001b\u001a\u00020\u0015J\u000e\u0010\u001c\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u0015J\u000e\u0010\u001e\u001a\u00020\u00132\u0006\u0010\u001f\u001a\u00020\u0015R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/example/madclass01/presentation/login/viewmodel/LoginViewModel;", "Landroidx/lifecycle/ViewModel;", "loginUseCase", "Lcom/example/madclass01/domain/usecase/LoginUseCase;", "validateEmailUseCase", "Lcom/example/madclass01/domain/usecase/ValidateEmailUseCase;", "validatePasswordUseCase", "Lcom/example/madclass01/domain/usecase/ValidatePasswordUseCase;", "backendRepository", "Lcom/example/madclass01/data/repository/BackendRepository;", "(Lcom/example/madclass01/domain/usecase/LoginUseCase;Lcom/example/madclass01/domain/usecase/ValidateEmailUseCase;Lcom/example/madclass01/domain/usecase/ValidatePasswordUseCase;Lcom/example/madclass01/data/repository/BackendRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/madclass01/presentation/login/viewmodel/LoginUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "handleKakaoLoginSuccess", "", "kakaoUserId", "", "nickname", "profileImageUrl", "login", "resetLoginState", "setLoginError", "message", "updateEmail", "newEmail", "updatePassword", "newPassword", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LoginViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.LoginUseCase loginUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.ValidateEmailUseCase validateEmailUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.ValidatePasswordUseCase validatePasswordUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.data.repository.BackendRepository backendRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.madclass01.presentation.login.viewmodel.LoginUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.login.viewmodel.LoginUiState> uiState = null;
    
    @javax.inject.Inject()
    public LoginViewModel(@org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.LoginUseCase loginUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.ValidateEmailUseCase validateEmailUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.ValidatePasswordUseCase validatePasswordUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.repository.BackendRepository backendRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.login.viewmodel.LoginUiState> getUiState() {
        return null;
    }
    
    public final void updateEmail(@org.jetbrains.annotations.NotNull()
    java.lang.String newEmail) {
    }
    
    public final void updatePassword(@org.jetbrains.annotations.NotNull()
    java.lang.String newPassword) {
    }
    
    public final void login() {
    }
    
    /**
     * 카카오 로그인 성공 후 백엔드에 사용자 등록/조회
     */
    public final void handleKakaoLoginSuccess(@org.jetbrains.annotations.NotNull()
    java.lang.String kakaoUserId, @org.jetbrains.annotations.Nullable()
    java.lang.String nickname, @org.jetbrains.annotations.Nullable()
    java.lang.String profileImageUrl) {
    }
    
    /**
     * 로그인 에러 설정 (외부에서 호출)
     */
    public final void setLoginError(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    public final void resetLoginState() {
    }
}