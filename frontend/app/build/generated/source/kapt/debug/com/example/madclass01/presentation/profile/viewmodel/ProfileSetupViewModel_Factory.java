package com.example.madclass01.presentation.profile.viewmodel;

import com.example.madclass01.data.repository.BackendRepository;
import com.example.madclass01.domain.usecase.AddImageUseCase;
import com.example.madclass01.domain.usecase.AddTagUseCase;
import com.example.madclass01.domain.usecase.RemoveImageUseCase;
import com.example.madclass01.domain.usecase.RemoveTagUseCase;
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
public final class ProfileSetupViewModel_Factory implements Factory<ProfileSetupViewModel> {
  private final Provider<AddImageUseCase> addImageUseCaseProvider;

  private final Provider<RemoveImageUseCase> removeImageUseCaseProvider;

  private final Provider<AddTagUseCase> addTagUseCaseProvider;

  private final Provider<RemoveTagUseCase> removeTagUseCaseProvider;

  private final Provider<BackendRepository> backendRepositoryProvider;

  public ProfileSetupViewModel_Factory(Provider<AddImageUseCase> addImageUseCaseProvider,
      Provider<RemoveImageUseCase> removeImageUseCaseProvider,
      Provider<AddTagUseCase> addTagUseCaseProvider,
      Provider<RemoveTagUseCase> removeTagUseCaseProvider,
      Provider<BackendRepository> backendRepositoryProvider) {
    this.addImageUseCaseProvider = addImageUseCaseProvider;
    this.removeImageUseCaseProvider = removeImageUseCaseProvider;
    this.addTagUseCaseProvider = addTagUseCaseProvider;
    this.removeTagUseCaseProvider = removeTagUseCaseProvider;
    this.backendRepositoryProvider = backendRepositoryProvider;
  }

  @Override
  public ProfileSetupViewModel get() {
    return newInstance(addImageUseCaseProvider.get(), removeImageUseCaseProvider.get(), addTagUseCaseProvider.get(), removeTagUseCaseProvider.get(), backendRepositoryProvider.get());
  }

  public static ProfileSetupViewModel_Factory create(
      Provider<AddImageUseCase> addImageUseCaseProvider,
      Provider<RemoveImageUseCase> removeImageUseCaseProvider,
      Provider<AddTagUseCase> addTagUseCaseProvider,
      Provider<RemoveTagUseCase> removeTagUseCaseProvider,
      Provider<BackendRepository> backendRepositoryProvider) {
    return new ProfileSetupViewModel_Factory(addImageUseCaseProvider, removeImageUseCaseProvider, addTagUseCaseProvider, removeTagUseCaseProvider, backendRepositoryProvider);
  }

  public static ProfileSetupViewModel newInstance(AddImageUseCase addImageUseCase,
      RemoveImageUseCase removeImageUseCase, AddTagUseCase addTagUseCase,
      RemoveTagUseCase removeTagUseCase, BackendRepository backendRepository) {
    return new ProfileSetupViewModel(addImageUseCase, removeImageUseCase, addTagUseCase, removeTagUseCase, backendRepository);
  }
}
