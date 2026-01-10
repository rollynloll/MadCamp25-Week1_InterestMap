package com.example.madclass01.domain.usecase;

import com.example.madclass01.domain.repository.LoginRepository;
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
public final class ValidatePasswordUseCase_Factory implements Factory<ValidatePasswordUseCase> {
  private final Provider<LoginRepository> loginRepositoryProvider;

  public ValidatePasswordUseCase_Factory(Provider<LoginRepository> loginRepositoryProvider) {
    this.loginRepositoryProvider = loginRepositoryProvider;
  }

  @Override
  public ValidatePasswordUseCase get() {
    return newInstance(loginRepositoryProvider.get());
  }

  public static ValidatePasswordUseCase_Factory create(
      Provider<LoginRepository> loginRepositoryProvider) {
    return new ValidatePasswordUseCase_Factory(loginRepositoryProvider);
  }

  public static ValidatePasswordUseCase newInstance(LoginRepository loginRepository) {
    return new ValidatePasswordUseCase(loginRepository);
  }
}
