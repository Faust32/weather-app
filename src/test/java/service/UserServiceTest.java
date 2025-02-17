package service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.faust.dto.UserRegistrationDTO;
import ru.faust.exception.AlreadyExistsException;
import ru.faust.exception.IncorrectInputDataException;
import ru.faust.exception.NotFoundModelException;
import ru.faust.model.Session;
import ru.faust.model.User;
import ru.faust.repository.UserRepository;
import ru.faust.service.AuthService;
import ru.faust.service.SessionService;
import ru.faust.util.PasswordHasher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_success() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        UserRegistrationDTO dto = new UserRegistrationDTO(user, "password");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.hash("password")).thenReturn("hashedPassword");

            authService.register(dto);

            assertEquals("hashedPassword", user.getPassword());
        }
    }

    @Test
    void registerUser_userExists() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        UserRegistrationDTO dto = new UserRegistrationDTO(user, "password");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> authService.register(dto));

        assertEquals("User with this username already exists.", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    void registerUser_passwordNotMatch() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        UserRegistrationDTO dto = new UserRegistrationDTO(user, "password1");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        IncorrectInputDataException exception = assertThrows(IncorrectInputDataException.class, () -> authService.register(dto));

        assertEquals("Passwords do not match.", exception.getMessage());

        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticateUser_success() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        Session expectedSession = new Session();
        when(sessionService.createSession(user, mockRequest)).thenReturn(expectedSession);

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verify(user.getPassword(), "password")).thenReturn(true);

            Session actualSession = authService.authenticate(user, mockRequest);

            assertEquals(expectedSession, actualSession);

            verify(sessionService).createSession(user, mockRequest);

        }
    }

    @Test
    void authenticateUser_passwordNotMatch() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        try (MockedStatic<PasswordHasher> mockedHasher = Mockito.mockStatic(PasswordHasher.class)) {
            mockedHasher.when(() -> PasswordHasher.verify(user.getPassword(), "password123")).thenReturn(true);

            IncorrectInputDataException exception = assertThrows(IncorrectInputDataException.class, () -> authService.authenticate(user, mockRequest));

            assertEquals("Incorrect password.", exception.getMessage());

            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void authenticateUser_noSuchUser() {
        User user = new User();
        user.setUsername("username");
        user.setPassword("password");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        NotFoundModelException exception = assertThrows(NotFoundModelException.class, () -> authService.authenticate(user, mockRequest));

        assertEquals("There is no such user with login username", exception.getMessage());
    }
}
