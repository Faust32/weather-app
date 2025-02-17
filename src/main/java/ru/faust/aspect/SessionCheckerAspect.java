package ru.faust.aspect;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.faust.exception.SessionExpiredException;
import ru.faust.model.Session;
import ru.faust.repository.SessionRepository;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
@Aspect
public class SessionCheckerAspect {

    private static final Logger logger = LoggerFactory.getLogger(SessionCheckerAspect.class);

    private final SessionRepository sessionRepository;

    public SessionCheckerAspect(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Pointcut("@annotation(CheckSession)")
    public void methodsWithVerification() {
    }

    @Before("methodsWithVerification()")
    public void checkSession() {
        logger.info("Checking users session...");
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            logger.error("No cookies found in the request.");
            throw new SessionExpiredException("You are not authorized to access this website. Please sign in first.");
        }
        String sessionId = Arrays.stream(cookies)
                .filter(c -> "sessionId".equals(c.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        if (sessionId == null) {
            logger.error("Session id not found.");
            throw new SessionExpiredException("You are not authorized to access this website. Please sign in first.");
        }
        Optional<Session> maybeSession = sessionRepository.findById(UUID.fromString(sessionId));
        if (maybeSession.isPresent()) {
            Session session = maybeSession.get();
            if (session.getExpiresAt().isBefore(Instant.now())) {
                logger.error("Session {} expired.", sessionId);
                throw new SessionExpiredException("Your session has expired. Please sign in first.");
            }
        }
    }
}
