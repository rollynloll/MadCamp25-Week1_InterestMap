package com.example.madclass01.presentation.profile.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\b\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B/\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u000e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\"\u0010\u0018\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u00172\b\b\u0002\u0010\u001a\u001a\u00020\u00172\b\b\u0002\u0010\u001b\u001a\u00020\u001cJ\u000e\u0010\u001d\u001a\u00020\u00152\u0006\u0010\u001e\u001a\u00020\u0017J\u0006\u0010\u001f\u001a\u00020\u0015J\u001a\u0010 \u001a\u0004\u0018\u00010!2\u0006\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020%H\u0002J\u000e\u0010&\u001a\u00020\u00152\u0006\u0010\"\u001a\u00020#J\u000e\u0010\'\u001a\u00020\u00152\u0006\u0010(\u001a\u00020\u0017J\u000e\u0010)\u001a\u00020\u00152\u0006\u0010\u0019\u001a\u00020\u0017J\u000e\u0010*\u001a\u00020\u00152\u0006\u0010(\u001a\u00020\u0017J\u0006\u0010+\u001a\u00020\u0015J\u000e\u0010,\u001a\u00020\u00152\u0006\u0010-\u001a\u00020\u0017J\u000e\u0010.\u001a\u00020\u00152\u0006\u0010/\u001a\u000200J\u000e\u00101\u001a\u00020\u00152\u0006\u00102\u001a\u00020\u0017J\u000e\u00103\u001a\u00020\u00152\u0006\u00104\u001a\u00020\u0017J\u000e\u00105\u001a\u00020\u00152\u0006\u00106\u001a\u00020\u0017R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013\u00a8\u00067"}, d2 = {"Lcom/example/madclass01/presentation/profile/viewmodel/ProfileSetupViewModel;", "Landroidx/lifecycle/ViewModel;", "addImageUseCase", "Lcom/example/madclass01/domain/usecase/AddImageUseCase;", "removeImageUseCase", "Lcom/example/madclass01/domain/usecase/RemoveImageUseCase;", "addTagUseCase", "Lcom/example/madclass01/domain/usecase/AddTagUseCase;", "removeTagUseCase", "Lcom/example/madclass01/domain/usecase/RemoveTagUseCase;", "backendRepository", "Lcom/example/madclass01/data/repository/BackendRepository;", "(Lcom/example/madclass01/domain/usecase/AddImageUseCase;Lcom/example/madclass01/domain/usecase/RemoveImageUseCase;Lcom/example/madclass01/domain/usecase/AddTagUseCase;Lcom/example/madclass01/domain/usecase/RemoveTagUseCase;Lcom/example/madclass01/data/repository/BackendRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/madclass01/presentation/profile/viewmodel/ProfileSetupUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "addHobby", "", "hobbyName", "", "addImage", "imageUri", "imageName", "imageSize", "", "addInterest", "interestName", "clearErrorMessage", "copyToTempFile", "Ljava/io/File;", "context", "Landroid/content/Context;", "image", "Lcom/example/madclass01/domain/model/ImageItem;", "proceedToNextStep", "removeHobby", "tagId", "removeImage", "removeInterest", "resetCompleteState", "setUserId", "userId", "updateAge", "newAge", "", "updateBio", "newBio", "updateNickname", "newNickname", "updateRegion", "newRegion", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ProfileSetupViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.AddImageUseCase addImageUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.RemoveImageUseCase removeImageUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.AddTagUseCase addTagUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.RemoveTagUseCase removeTagUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.data.repository.BackendRepository backendRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.madclass01.presentation.profile.viewmodel.ProfileSetupUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.profile.viewmodel.ProfileSetupUiState> uiState = null;
    
    @javax.inject.Inject()
    public ProfileSetupViewModel(@org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.AddImageUseCase addImageUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.RemoveImageUseCase removeImageUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.AddTagUseCase addTagUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.RemoveTagUseCase removeTagUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.data.repository.BackendRepository backendRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.profile.viewmodel.ProfileSetupUiState> getUiState() {
        return null;
    }
    
    /**
     * 로그인 후 userId 설정
     */
    public final void setUserId(@org.jetbrains.annotations.NotNull()
    java.lang.String userId) {
    }
    
    public final void updateNickname(@org.jetbrains.annotations.NotNull()
    java.lang.String newNickname) {
    }
    
    public final void updateAge(int newAge) {
    }
    
    public final void updateRegion(@org.jetbrains.annotations.NotNull()
    java.lang.String newRegion) {
    }
    
    public final void updateBio(@org.jetbrains.annotations.NotNull()
    java.lang.String newBio) {
    }
    
    public final void addImage(@org.jetbrains.annotations.NotNull()
    java.lang.String imageUri, @org.jetbrains.annotations.NotNull()
    java.lang.String imageName, long imageSize) {
    }
    
    public final void removeImage(@org.jetbrains.annotations.NotNull()
    java.lang.String imageUri) {
    }
    
    public final void addHobby(@org.jetbrains.annotations.NotNull()
    java.lang.String hobbyName) {
    }
    
    public final void removeHobby(@org.jetbrains.annotations.NotNull()
    java.lang.String tagId) {
    }
    
    public final void addInterest(@org.jetbrains.annotations.NotNull()
    java.lang.String interestName) {
    }
    
    public final void removeInterest(@org.jetbrains.annotations.NotNull()
    java.lang.String tagId) {
    }
    
    public final void proceedToNextStep(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void resetCompleteState() {
    }
    
    public final void clearErrorMessage() {
    }
    
    private final java.io.File copyToTempFile(android.content.Context context, com.example.madclass01.domain.model.ImageItem image) {
        return null;
    }
}