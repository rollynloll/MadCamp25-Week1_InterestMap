package com.example.madclass01.presentation.profile.viewmodel;

import com.example.madclass01.domain.usecase.AddTagUseCase;
import com.example.madclass01.domain.usecase.AnalyzeImagesUseCase;
import com.example.madclass01.domain.usecase.RemoveTagUseCase;
import com.example.madclass01.domain.usecase.ToggleTagUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class TagSelectionViewModel_Factory implements Factory<TagSelectionViewModel> {
  private final Provider<AnalyzeImagesUseCase> analyzeImagesUseCaseProvider;

  private final Provider<AddTagUseCase> addTagUseCaseProvider;

  private final Provider<RemoveTagUseCase> removeTagUseCaseProvider;

  private final Provider<ToggleTagUseCase> toggleTagUseCaseProvider;

  public TagSelectionViewModel_Factory(Provider<AnalyzeImagesUseCase> analyzeImagesUseCaseProvider,
      Provider<AddTagUseCase> addTagUseCaseProvider,
      Provider<RemoveTagUseCase> removeTagUseCaseProvider,
      Provider<ToggleTagUseCase> toggleTagUseCaseProvider) {
    this.analyzeImagesUseCaseProvider = analyzeImagesUseCaseProvider;
    this.addTagUseCaseProvider = addTagUseCaseProvider;
    this.removeTagUseCaseProvider = removeTagUseCaseProvider;
    this.toggleTagUseCaseProvider = toggleTagUseCaseProvider;
  }

  @Override
  public TagSelectionViewModel get() {
    return newInstance(analyzeImagesUseCaseProvider.get(), addTagUseCaseProvider.get(), removeTagUseCaseProvider.get(), toggleTagUseCaseProvider.get());
  }

  public static TagSelectionViewModel_Factory create(
      Provider<AnalyzeImagesUseCase> analyzeImagesUseCaseProvider,
      Provider<AddTagUseCase> addTagUseCaseProvider,
      Provider<RemoveTagUseCase> removeTagUseCaseProvider,
      Provider<ToggleTagUseCase> toggleTagUseCaseProvider) {
    return new TagSelectionViewModel_Factory(analyzeImagesUseCaseProvider, addTagUseCaseProvider, removeTagUseCaseProvider, toggleTagUseCaseProvider);
  }

  public static TagSelectionViewModel newInstance(AnalyzeImagesUseCase analyzeImagesUseCase,
      AddTagUseCase addTagUseCase, RemoveTagUseCase removeTagUseCase,
      ToggleTagUseCase toggleTagUseCase) {
    return new TagSelectionViewModel(analyzeImagesUseCase, addTagUseCase, removeTagUseCase, toggleTagUseCase);
  }
}
