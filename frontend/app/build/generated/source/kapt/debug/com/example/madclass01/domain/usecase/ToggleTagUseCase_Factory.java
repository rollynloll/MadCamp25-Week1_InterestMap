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
public final class ToggleTagUseCase_Factory implements Factory<ToggleTagUseCase> {
  @Override
  public ToggleTagUseCase get() {
    return newInstance();
  }

  public static ToggleTagUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ToggleTagUseCase newInstance() {
    return new ToggleTagUseCase();
  }

  private static final class InstanceHolder {
    private static final ToggleTagUseCase_Factory INSTANCE = new ToggleTagUseCase_Factory();
  }
}
