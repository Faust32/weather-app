package ru.faust.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.faust.dto.UserRegistrationDTO;
import ru.faust.model.Session;
import ru.faust.model.User;
import ru.faust.service.AuthService;
import ru.faust.service.SessionService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    private final SessionService sessionService;

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @GetMapping("/sign-in")
    public String signInPage() {
        logger.info("Processing sign-in page request.");
        return "sign-in";
    }

    @GetMapping("/sign-up")
    public String registrationPage() {
        logger.info("Processing sign-up page request.");
        return "sign-up";
    }

    @PostMapping("/sign-in")
    public String signIn(@ModelAttribute User user, HttpServletRequest request, HttpServletResponse response) {
        logger.info("Authenticating user: {} ", user.getUsername());
        Session session = authService.authenticate(user, request);
        sessionService.setSessionCookie(response, session);
        return "redirect:/home";
    }

    @PostMapping("/sign-up")
    public String signUp(@ModelAttribute User user, @RequestParam("repeat-password") String repeatPassword) {
        logger.info("User {} is attempting to register", user.getUsername());
        authService.register(new UserRegistrationDTO(user, repeatPassword));
        logger.info("User {} successfully registered.", user.getUsername());
        return "redirect:/auth/sign-in";
    }

}
