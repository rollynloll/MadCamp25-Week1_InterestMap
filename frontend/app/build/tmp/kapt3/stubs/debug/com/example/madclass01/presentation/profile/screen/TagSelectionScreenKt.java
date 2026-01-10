package com.example.madclass01.presentation.profile.screen;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000:\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a*\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u001a6\u0010\b\u001a\u00020\u00012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00072\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u001a\u0080\u0001\u0010\u000b\u001a\u00020\u00012\b\u0010\f\u001a\u0004\u0018\u00010\u00072\u0006\u0010\r\u001a\u00020\u00072\b\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00072\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00070\u00032\b\b\u0002\u0010\u0012\u001a\u00020\u00132\u000e\b\u0002\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u00152#\b\u0002\u0010\u0016\u001a\u001d\u0012\u0013\u0012\u00110\u000f\u00a2\u0006\f\b\u0017\u0012\b\b\u0018\u0012\u0004\b\b(\u0019\u0012\u0004\u0012\u00020\u00010\u0006H\u0007\u00a2\u0006\u0002\u0010\u001a\u00a8\u0006\u001b"}, d2 = {"CustomTagSection", "", "tags", "", "Lcom/example/madclass01/domain/model/Tag;", "onRemoveTag", "Lkotlin/Function1;", "", "TagSection", "title", "onToggleTag", "TagSelectionScreen", "userId", "nickname", "age", "", "region", "recommendedTags", "viewModel", "Lcom/example/madclass01/presentation/profile/viewmodel/TagSelectionViewModel;", "onBack", "Lkotlin/Function0;", "onComplete", "Lkotlin/ParameterName;", "name", "selectedTags", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/util/List;Lcom/example/madclass01/presentation/profile/viewmodel/TagSelectionViewModel;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;)V", "app_debug"})
public final class TagSelectionScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void TagSelectionScreen(@org.jetbrains.annotations.Nullable()
    java.lang.String userId, @org.jetbrains.annotations.NotNull()
    java.lang.String nickname, @org.jetbrains.annotations.Nullable()
    java.lang.Integer age, @org.jetbrains.annotations.Nullable()
    java.lang.String region, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> recommendedTags, @org.jetbrains.annotations.NotNull()
    com.example.madclass01.presentation.profile.viewmodel.TagSelectionViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onComplete) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TagSection(@org.jetbrains.annotations.Nullable()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.madclass01.domain.model.Tag> tags, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onToggleTag) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CustomTagSection(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.madclass01.domain.model.Tag> tags, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onRemoveTag) {
    }
}