package ru.faust.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.faust.aspect.CheckSession;
import ru.faust.service.UserForecastService;
import ru.faust.service.UserLocationsService;
import ru.faust.service.SessionService;

@Controller
@RequestMapping("/home")
public class HomePageController {

    private static final Logger logger = LoggerFactory.getLogger(HomePageController.class);

    private final UserForecastService userForecastService;

    private final UserLocationsService userLocationsService;

    private final SessionService sessionService;

    public HomePageController(UserForecastService userForecastService, UserLocationsService userLocationsService, SessionService sessionService) {
        this.userForecastService = userForecastService;
        this.userLocationsService = userLocationsService;
        this.sessionService = sessionService;
    }

    @CheckSession
    @GetMapping
    public String homePage(Model model) {
        model.addAttribute("usersSavedLocations", userForecastService.findAllSavedForecast());
        return "index";
    }

    @CheckSession
    @DeleteMapping("/delete-location")
    public String deleteLocation(@RequestParam(name = "locationId") Long locationId) {
        logger.info("Delete location {}", locationId);
        userLocationsService.removeSavedLocation(locationId);
        return "redirect:/home";
    }

    @CheckSession
    @DeleteMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("User {} is logging out.", sessionService.getCurrentUserSession().getUser().getUsername());
        sessionService.logout(request, response);
        logger.info("User successfully logged out.");
        return "redirect:/auth/sign-in";
    }

}
