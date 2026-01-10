package com.example.madclass01.presentation.search.viewmodel;

import com.example.madclass01.domain.usecase.SearchGroupsUseCase;
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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<SearchGroupsUseCase> searchGroupsUseCaseProvider;

  public SearchViewModel_Factory(Provider<SearchGroupsUseCase> searchGroupsUseCaseProvider) {
    this.searchGroupsUseCaseProvider = searchGroupsUseCaseProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(searchGroupsUseCaseProvider.get());
  }

  public static SearchViewModel_Factory create(
      Provider<SearchGroupsUseCase> searchGroupsUseCaseProvider) {
    return new SearchViewModel_Factory(searchGroupsUseCaseProvider);
  }

  public static SearchViewModel newInstance(SearchGroupsUseCase searchGroupsUseCase) {
    return new SearchViewModel(searchGroupsUseCase);
  }
}
