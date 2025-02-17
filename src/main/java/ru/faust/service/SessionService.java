package ru.faust.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.faust.exception.SessionExpiredException;
import ru.faust.model.Session;
import ru.faust.model.User;
import ru.faust.repository.SessionRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;

    public Session createSession(User authenticatedUser, HttpServletRequest request) {
        logger.info("Creating new session for user: {} ", authenticatedUser.getUsername());
        sessionRepository.deleteByUser(authenticatedUser);

        Session session = Session.builder()
                .id(UUID.randomUUID())
                .user(authenticatedUser)
                .expiresAt(Instant.now().plusSeconds(2 * 60 * 60)) // 2 hours
                .build();

        sessionRepository.saveAndFlush(session);

        request.getSession().invalidate();
        HttpSession httpSession = request.getSession(true);
        httpSession.setAttribute("user", authenticatedUser);
        httpSession.setAttribute("session", session);

        return session;
    }

    public void setSessionCookie(HttpServletResponse response, Session session) {

        Cookie cookie = new Cookie("sessionId", session.getId().toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(2 * 60 * 60); // 2 hours

        response.addCookie(cookie);

    }

    public Session getCurrentUserSession() {
        logger.info("Getting session id for current user.");
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            logger.error("Session id not found");
            throw new SessionExpiredException("Session ID not found.");
        }

        String sessionId = Arrays.stream(cookies)
                .filter(c -> "sessionId".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Session id not found");
                    return new SessionExpiredException("Session ID not found.");
                });

        return sessionRepository.findById(UUID.fromString(sessionId))
                .orElseThrow(() -> {
                    logger.error("Session expired or invalid.");
                    return new SessionExpiredException("Session expired or invalid.");
                });
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Deleting session.");
        Session session = getCurrentUserSession();
        sessionRepository.delete(session);

        logger.info("Deleting cookie.");
        Cookie cookie = new Cookie("sessionId", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        request.getSession().invalidate();
    }
}
