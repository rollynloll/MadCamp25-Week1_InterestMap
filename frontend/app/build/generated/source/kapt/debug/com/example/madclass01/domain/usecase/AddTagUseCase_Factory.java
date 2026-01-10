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
public final class AddTagUseCase_Factory implements Factory<AddTagUseCase> {
  @Override
  public AddTagUseCase get() {
    return newInstance();
  }

  public static AddTagUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AddTagUseCase newInstance() {
    return new AddTagUseCase();
  }

  private static final class InstanceHolder {
    private static final AddTagUseCase_Factory INSTANCE = new AddTagUseCase_Factory();
  }
}
