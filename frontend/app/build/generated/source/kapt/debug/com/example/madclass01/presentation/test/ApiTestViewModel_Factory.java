package com.example.madclass01.presentation.test;

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
public final class ApiTestViewModel_Factory implements Factory<ApiTestViewModel> {
  private final Provider<BackendRepository> backendRepositoryProvider;

  public ApiTestViewModel_Factory(Provider<BackendRepository> backendRepositoryProvider) {
    this.backendRepositoryProvider = backendRepositoryProvider;
  }

  @Override
  public ApiTestViewModel get() {
    return newInstance(backendRepositoryProvider.get());
  }

  public static ApiTestViewModel_Factory create(
      Provider<BackendRepository> backendRepositoryProvider) {
    return new ApiTestViewModel_Factory(backendRepositoryProvider);
  }

  public static ApiTestViewModel newInstance(BackendRepository backendRepository) {
    return new ApiTestViewModel(backendRepository);
  }
}
