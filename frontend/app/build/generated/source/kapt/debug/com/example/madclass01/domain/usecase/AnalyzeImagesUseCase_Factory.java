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
public final class AnalyzeImagesUseCase_Factory implements Factory<AnalyzeImagesUseCase> {
  @Override
  public AnalyzeImagesUseCase get() {
    return newInstance();
  }

  public static AnalyzeImagesUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AnalyzeImagesUseCase newInstance() {
    return new AnalyzeImagesUseCase();
  }

  private static final class InstanceHolder {
    private static final AnalyzeImagesUseCase_Factory INSTANCE = new AnalyzeImagesUseCase_Factory();
  }
}
