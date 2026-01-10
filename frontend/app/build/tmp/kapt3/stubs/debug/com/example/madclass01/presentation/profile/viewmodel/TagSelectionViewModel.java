package com.example.madclass01.presentation.profile.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u0001B\'\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u000e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u0015J\u0006\u0010\u0016\u001a\u00020\u0013J\u0006\u0010\u0017\u001a\u00020\u0013J\u0006\u0010\u0018\u001a\u00020\u0013J\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001aJ\u000e\u0010\u001c\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u0015J\u0006\u0010\u001e\u001a\u00020\u0013J\u0014\u0010\u001f\u001a\u00020\u00132\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00150\u001aJ\u000e\u0010!\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u0015J\u000e\u0010\"\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u0015R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\r0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011\u00a8\u0006#"}, d2 = {"Lcom/example/madclass01/presentation/profile/viewmodel/TagSelectionViewModel;", "Landroidx/lifecycle/ViewModel;", "analyzeImagesUseCase", "Lcom/example/madclass01/domain/usecase/AnalyzeImagesUseCase;", "addTagUseCase", "Lcom/example/madclass01/domain/usecase/AddTagUseCase;", "removeTagUseCase", "Lcom/example/madclass01/domain/usecase/RemoveTagUseCase;", "toggleTagUseCase", "Lcom/example/madclass01/domain/usecase/ToggleTagUseCase;", "(Lcom/example/madclass01/domain/usecase/AnalyzeImagesUseCase;Lcom/example/madclass01/domain/usecase/AddTagUseCase;Lcom/example/madclass01/domain/usecase/RemoveTagUseCase;Lcom/example/madclass01/domain/usecase/ToggleTagUseCase;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/madclass01/presentation/profile/viewmodel/TagSelectionUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "addCustomTag", "", "tagName", "", "analyzeImages", "clearErrorMessage", "completeSelection", "getSelectedTags", "", "Lcom/example/madclass01/domain/model/Tag;", "removeCustomTag", "tagId", "resetCompleteState", "setRecommendedTags", "tags", "toggleExtractedTag", "toggleRecommendedTag", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class TagSelectionViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.AnalyzeImagesUseCase analyzeImagesUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.AddTagUseCase addTagUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.RemoveTagUseCase removeTagUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.madclass01.domain.usecase.ToggleTagUseCase toggleTagUseCase = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.madclass01.presentation.profile.viewmodel.TagSelectionUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.profile.viewmodel.TagSelectionUiState> uiState = null;
    
    @javax.inject.Inject()
    public TagSelectionViewModel(@org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.AnalyzeImagesUseCase analyzeImagesUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.AddTagUseCase addTagUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.RemoveTagUseCase removeTagUseCase, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.domain.usecase.ToggleTagUseCase toggleTagUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.madclass01.presentation.profile.viewmodel.TagSelectionUiState> getUiState() {
        return null;
    }
    
    public final void analyzeImages() {
    }
    
    public final void setRecommendedTags(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> tags) {
    }
    
    public final void toggleExtractedTag(@org.jetbrains.annotations.NotNull()
    java.lang.String tagId) {
    }
    
    public final void toggleRecommendedTag(@org.jetbrains.annotations.NotNull()
    java.lang.String tagId) {
    }
    
    public final void addCustomTag(@org.jetbrains.annotations.NotNull()
    java.lang.String tagName) {
    }
    
    public final void removeCustomTag(@org.jetbrains.annotations.NotNull()
    java.lang.String tagId) {
    }
    
    public final void completeSelection() {
    }
    
    public final void resetCompleteState() {
    }
    
    public final void clearErrorMessage() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.madclass01.domain.model.Tag> getSelectedTags() {
        return null;
    }
}