package com.example.madclass01.data.repository;

import com.example.madclass01.data.remote.ApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class BackendRepository_Factory implements Factory<BackendRepository> {
  private final Provider<ApiService> apiServiceProvider;

  public BackendRepository_Factory(Provider<ApiService> apiServiceProvider) {
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public BackendRepository get() {
    return newInstance(apiServiceProvider.get());
  }

  public static BackendRepository_Factory create(Provider<ApiService> apiServiceProvider) {
    return new BackendRepository_Factory(apiServiceProvider);
  }

  public static BackendRepository newInstance(ApiService apiService) {
    return new BackendRepository(apiService);
  }
}
