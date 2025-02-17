package ru.faust.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.faust.dto.UserRegistrationDTO;
import ru.faust.exception.AlreadyExistsException;
import ru.faust.exception.IncorrectInputDataException;
import ru.faust.exception.NotFoundModelException;
import ru.faust.model.Session;
import ru.faust.model.User;
import ru.faust.repository.UserRepository;
import ru.faust.util.PasswordHasher;
import ru.faust.util.UserValidation;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;

    private final SessionService sessionService;

    public Session authenticate(User user, HttpServletRequest request) {
        logger.info("Checking if user {} exists in DB.", user.getUsername());
        Optional<User> savedUser = userRepository.findByUsername(user.getUsername());
        if (savedUser.isPresent()) {
            if (PasswordHasher.verify(user.getPassword(), savedUser.get().getPassword())) {
                logger.info("User {} successfully logged in.", user.getUsername());
                return sessionService.createSession(savedUser.get(), request);
            } else {
                logger.error("Incorrect password.");
                throw new IncorrectInputDataException("Incorrect password.", "authenticate");
            }
        } else {
            logger.error("User {} does not exist.", user.getUsername());
            throw new NotFoundModelException("There is no such user with login " + user.getUsername(), "authenticate");
        }
    }

    public void register(UserRegistrationDTO userRegistration) {
        logger.info("Checking if user {} exists in DB before registration.", userRegistration.user().getUsername());
        User user = userRegistration.user();
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            logger.error("User {} already exists.", user.getUsername());
            throw new AlreadyExistsException("User with this username already exists.", "register");
        }
        UserValidation.validateUser(user);
        String password = user.getPassword();
        if (password.equals(userRegistration.repeatPassword())) {
            logger.info("Saving user {} in database.", user.getUsername());
            user.setPassword(PasswordHasher.hash(password));
            user.setLocations(new ArrayList<>());
            userRepository.save(user);
        } else {
            logger.error("Passwords do not match.");
            throw new IncorrectInputDataException("Passwords do not match.", "register");
        }
    }
}
