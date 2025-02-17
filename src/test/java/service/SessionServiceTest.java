package service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.faust.exception.SessionExpiredException;
import ru.faust.model.Session;
import ru.faust.repository.SessionRepository;
import ru.faust.service.SessionService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionService sessionService;

    private MockHttpServletRequest request;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void getCurrentUserSession_success() {
        UUID sessionId = UUID.randomUUID();
        Session expectedSession = new Session();
        expectedSession.setId(sessionId);

        request.setCookies(new MockCookie("sessionId", sessionId.toString()));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(expectedSession));
        Session result = sessionService.getCurrentUserSession();

        assertThat(result).isEqualTo(expectedSession);
    }

    @Test
    void getCurrentUserSession_sessionExpired() {
        UUID sessionId = UUID.randomUUID();

        request.setCookies(new MockCookie("sessionId", sessionId.toString()));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        SessionExpiredException exception = assertThrows(SessionExpiredException.class, () -> sessionService.getCurrentUserSession());
        assertThat(exception.getMessage()).isEqualTo("Session expired or invalid.");
    }

    @Test
    void getCurrentUserSession_notFound() {
        request.setCookies(new MockCookie("someCookie", "dummyValue"));

        SessionExpiredException exception = assertThrows(SessionExpiredException.class, () -> sessionService.getCurrentUserSession());

        assertThat(exception.getMessage()).isEqualTo("Session ID not found.");
    }

    @Test
    void logout_success() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession mockSession = mock(HttpSession.class);
        Session session = new Session();
        session.setId(UUID.randomUUID());

        MockCookie mockCookie = new MockCookie("sessionId", session.getId().toString());
        request.setCookies(mockCookie);
        request.setSession(mockSession);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(sessionRepository.findById(session.getId())).thenReturn(Optional.of(session));

        sessionService.logout(request, response);

        verify(sessionRepository).delete(session);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());

        Cookie clearedCookie = cookieCaptor.getValue();
        assertThat(clearedCookie.getName()).isEqualTo("sessionId");
        assertThat(clearedCookie.getValue()).isNullOrEmpty();
        assertThat(clearedCookie.getMaxAge()).isZero();
        assertThat(clearedCookie.getPath()).isEqualTo("/");
        assertThat(clearedCookie.isHttpOnly()).isTrue();

        verify(mockSession).invalidate();
        RequestContextHolder.resetRequestAttributes();
    }

}
