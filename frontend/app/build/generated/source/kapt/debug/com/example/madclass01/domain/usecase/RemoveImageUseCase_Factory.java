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
public final class RemoveImageUseCase_Factory implements Factory<RemoveImageUseCase> {
  @Override
  public RemoveImageUseCase get() {
    return newInstance();
  }

  public static RemoveImageUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RemoveImageUseCase newInstance() {
    return new RemoveImageUseCase();
  }

  private static final class InstanceHolder {
    private static final RemoveImageUseCase_Factory INSTANCE = new RemoveImageUseCase_Factory();
  }
}
