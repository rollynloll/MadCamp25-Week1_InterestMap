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
public final class ValidateEmailUseCase_Factory implements Factory<ValidateEmailUseCase> {
  private final Provider<LoginRepository> loginRepositoryProvider;

  public ValidateEmailUseCase_Factory(Provider<LoginRepository> loginRepositoryProvider) {
    this.loginRepositoryProvider = loginRepositoryProvider;
  }

  @Override
  public ValidateEmailUseCase get() {
    return newInstance(loginRepositoryProvider.get());
  }

  public static ValidateEmailUseCase_Factory create(
      Provider<LoginRepository> loginRepositoryProvider) {
    return new ValidateEmailUseCase_Factory(loginRepositoryProvider);
  }

  public static ValidateEmailUseCase newInstance(LoginRepository loginRepository) {
    return new ValidateEmailUseCase(loginRepository);
  }
}
