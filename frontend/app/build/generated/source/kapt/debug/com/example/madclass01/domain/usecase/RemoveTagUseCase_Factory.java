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
public final class RemoveTagUseCase_Factory implements Factory<RemoveTagUseCase> {
  @Override
  public RemoveTagUseCase get() {
    return newInstance();
  }

  public static RemoveTagUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RemoveTagUseCase newInstance() {
    return new RemoveTagUseCase();
  }

  private static final class InstanceHolder {
    private static final RemoveTagUseCase_Factory INSTANCE = new RemoveTagUseCase_Factory();
  }
}
