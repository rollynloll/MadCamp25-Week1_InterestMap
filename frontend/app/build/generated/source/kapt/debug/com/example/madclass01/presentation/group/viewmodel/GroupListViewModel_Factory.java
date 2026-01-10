package com.example.madclass01.presentation.group.viewmodel;

import com.example.madclass01.data.repository.BackendRepository;
import com.example.madclass01.domain.usecase.GetMyGroupsUseCase;
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
public final class GroupListViewModel_Factory implements Factory<GroupListViewModel> {
  private final Provider<GetMyGroupsUseCase> getMyGroupsUseCaseProvider;

  private final Provider<BackendRepository> backendRepositoryProvider;

  public GroupListViewModel_Factory(Provider<GetMyGroupsUseCase> getMyGroupsUseCaseProvider,
      Provider<BackendRepository> backendRepositoryProvider) {
    this.getMyGroupsUseCaseProvider = getMyGroupsUseCaseProvider;
    this.backendRepositoryProvider = backendRepositoryProvider;
  }

  @Override
  public GroupListViewModel get() {
    return newInstance(getMyGroupsUseCaseProvider.get(), backendRepositoryProvider.get());
  }

  public static GroupListViewModel_Factory create(
      Provider<GetMyGroupsUseCase> getMyGroupsUseCaseProvider,
      Provider<BackendRepository> backendRepositoryProvider) {
    return new GroupListViewModel_Factory(getMyGroupsUseCaseProvider, backendRepositoryProvider);
  }

  public static GroupListViewModel newInstance(GetMyGroupsUseCase getMyGroupsUseCase,
      BackendRepository backendRepository) {
    return new GroupListViewModel(getMyGroupsUseCase, backendRepository);
  }
}
