package com.example.madclass01.data.repository;

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
public final class LoginRepositoryImpl_Factory implements Factory<LoginRepositoryImpl> {
  @Override
  public LoginRepositoryImpl get() {
    return newInstance();
  }

  public static LoginRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LoginRepositoryImpl newInstance() {
    return new LoginRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final LoginRepositoryImpl_Factory INSTANCE = new LoginRepositoryImpl_Factory();
  }
}
