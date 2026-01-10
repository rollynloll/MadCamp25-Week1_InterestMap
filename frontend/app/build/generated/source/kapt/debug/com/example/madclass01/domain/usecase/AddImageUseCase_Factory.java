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
public final class AddImageUseCase_Factory implements Factory<AddImageUseCase> {
  @Override
  public AddImageUseCase get() {
    return newInstance();
  }

  public static AddImageUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AddImageUseCase newInstance() {
    return new AddImageUseCase();
  }

  private static final class InstanceHolder {
    private static final AddImageUseCase_Factory INSTANCE = new AddImageUseCase_Factory();
  }
}
