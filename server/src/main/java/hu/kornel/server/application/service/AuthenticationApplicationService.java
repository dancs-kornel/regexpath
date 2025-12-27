package hu.kornel.server.application.service;

import org.springframework.stereotype.Service;

import hu.kornel.server.application.dto.AuthenticationResponseDto;
import hu.kornel.server.application.dto.LoginCommand;
import hu.kornel.server.application.dto.RegisterUserCommand;
import hu.kornel.server.application.dto.UserDto;
import hu.kornel.server.application.usecase.GetCurrentUserUseCase;
import hu.kornel.server.application.usecase.LoginUserUseCase;
import hu.kornel.server.application.usecase.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationApplicationService {
    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    public AuthenticationResponseDto register(RegisterUserCommand command) { return registerUserUseCase.execute(command); }
    public AuthenticationResponseDto login(LoginCommand command) { return loginUserUseCase.execute(command); }
    public UserDto getCurrentUser(String token) { return getCurrentUserUseCase.execute(token); } 
}
