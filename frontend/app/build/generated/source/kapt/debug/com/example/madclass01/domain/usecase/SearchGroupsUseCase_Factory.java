package com.example.madclass01.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SearchGroupsUseCase_Factory implements Factory<SearchGroupsUseCase> {
  @Override
  public SearchGroupsUseCase get() {
    return newInstance();
  }

  public static SearchGroupsUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SearchGroupsUseCase newInstance() {
    return new SearchGroupsUseCase();
  }

  private static final class InstanceHolder {
    private static final SearchGroupsUseCase_Factory INSTANCE = new SearchGroupsUseCase_Factory();
  }
}
