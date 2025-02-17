package ru.faust.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SessionExpiredException.class)
    public ModelAndView handleSessionExpiredException(SessionExpiredException e) {
        logger.info("Handling session expired exception with this message: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView("sign-in-with-errors");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("errorType", "sessionExpired");
        logger.info("Redirecting to sign-in page with error message: {}", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ModelAndView handleAlreadyExistsException(AlreadyExistsException e) {
        logger.info("Handling already exists exception with this message: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView(determineViewForAlreadyExistsException(e));
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("errorType", "alreadyExists");
        logger.info("Redirecting to sign-up or error page with error message: {}", e.getMessage());
        return modelAndView;
    }

    private String determineViewForAlreadyExistsException(AlreadyExistsException e) {
        if ("register".equals(e.getMethodName())) {
            return "sign-up-with-errors";
        }
        return "error";
    }

    @ExceptionHandler(IncorrectInputDataException.class)
    public ModelAndView handleIncorrectInputDataException(IncorrectInputDataException e) {
        logger.info("Handling wrong input data exception with this message: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView(determineViewForIncorrectInputDataException(e));
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("errorType", "incorrectInputData");
        logger.info("Redirecting to sign-in or sign-up page with error message: {}", e.getMessage());
        return modelAndView;
    }

    private String determineViewForIncorrectInputDataException(IncorrectInputDataException e) {
        if ("authenticate".equals(e.getMethodName())) {
            return "sign-in-with-errors";
        } else if ("register".equals(e.getMethodName())) {
            return "sign-up-with-errors";
        }
        return "error";
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public ModelAndView handleInvalidParameterException(InvalidUsernameException e) {
        logger.info("Handling invalid username exception with this message: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView("sign-up-with-errors");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("errorType", "invalidUsername");
        logger.info("Redirecting to sign-up page with error message: {}", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ModelAndView handleInvalidParameterException(InvalidPasswordException e) {
        logger.info("Handling invalid password exception with this message: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView("sign-up-with-errors");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("errorType", "invalidPassword");
        logger.info("Redirecting to sign-up page with error message: {}", e.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(NotFoundModelException.class)
    public ModelAndView handleNotFoundModelException(NotFoundModelException e) {
        logger.info("Handling not found model exception with this message: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView(determineViewForNotFoundModelException(e));
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("errorType", "notFound");
        logger.info("Redirecting to sign-in or error page with error message: {}", e.getMessage());
        return modelAndView;
    }

    private String determineViewForNotFoundModelException(NotFoundModelException e) {
        if ("authenticate".equals(e.getMethodName())) {
            return "sign-in-with-errors";
        }
        return "error";
    }

    @ExceptionHandler(UtilityFilesLoadingException.class)
    public ModelAndView handleUtilityFilesLoadingException(UtilityFilesLoadingException e) {
        logger.info("Handling utility files loading exception with this message: {}", e.getMessage());
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("error", e.getMessage());
        modelAndView.addObject("errorType", "utilFilesLoading");
        logger.info("Redirecting to error page with error message: {}", e.getMessage());
        return modelAndView;
    }
}
