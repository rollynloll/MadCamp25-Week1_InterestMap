package com.example.madclass01.presentation.profile.viewmodel;

import com.example.madclass01.data.repository.BackendRepository;
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
public final class LoadingViewModel_Factory implements Factory<LoadingViewModel> {
  private final Provider<BackendRepository> backendRepositoryProvider;

  public LoadingViewModel_Factory(Provider<BackendRepository> backendRepositoryProvider) {
    this.backendRepositoryProvider = backendRepositoryProvider;
  }

  @Override
  public LoadingViewModel get() {
    return newInstance(backendRepositoryProvider.get());
  }

  public static LoadingViewModel_Factory create(
      Provider<BackendRepository> backendRepositoryProvider) {
    return new LoadingViewModel_Factory(backendRepositoryProvider);
  }

  public static LoadingViewModel newInstance(BackendRepository backendRepository) {
    return new LoadingViewModel(backendRepository);
  }
}
