package com.example.madclass01.presentation.login.viewmodel;

import com.example.madclass01.data.repository.BackendRepository;
import com.example.madclass01.domain.usecase.LoginUseCase;
import com.example.madclass01.domain.usecase.ValidateEmailUseCase;
import com.example.madclass01.domain.usecase.ValidatePasswordUseCase;
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
public final class LoginViewModel_Factory implements Factory<LoginViewModel> {
  private final Provider<LoginUseCase> loginUseCaseProvider;

  private final Provider<ValidateEmailUseCase> validateEmailUseCaseProvider;

  private final Provider<ValidatePasswordUseCase> validatePasswordUseCaseProvider;

  private final Provider<BackendRepository> backendRepositoryProvider;

  public LoginViewModel_Factory(Provider<LoginUseCase> loginUseCaseProvider,
      Provider<ValidateEmailUseCase> validateEmailUseCaseProvider,
      Provider<ValidatePasswordUseCase> validatePasswordUseCaseProvider,
      Provider<BackendRepository> backendRepositoryProvider) {
    this.loginUseCaseProvider = loginUseCaseProvider;
    this.validateEmailUseCaseProvider = validateEmailUseCaseProvider;
    this.validatePasswordUseCaseProvider = validatePasswordUseCaseProvider;
    this.backendRepositoryProvider = backendRepositoryProvider;
  }

  @Override
  public LoginViewModel get() {
    return newInstance(loginUseCaseProvider.get(), validateEmailUseCaseProvider.get(), validatePasswordUseCaseProvider.get(), backendRepositoryProvider.get());
  }

  public static LoginViewModel_Factory create(Provider<LoginUseCase> loginUseCaseProvider,
      Provider<ValidateEmailUseCase> validateEmailUseCaseProvider,
      Provider<ValidatePasswordUseCase> validatePasswordUseCaseProvider,
      Provider<BackendRepository> backendRepositoryProvider) {
    return new LoginViewModel_Factory(loginUseCaseProvider, validateEmailUseCaseProvider, validatePasswordUseCaseProvider, backendRepositoryProvider);
  }

  public static LoginViewModel newInstance(LoginUseCase loginUseCase,
      ValidateEmailUseCase validateEmailUseCase, ValidatePasswordUseCase validatePasswordUseCase,
      BackendRepository backendRepository) {
    return new LoginViewModel(loginUseCase, validateEmailUseCase, validatePasswordUseCase, backendRepository);
  }
}
