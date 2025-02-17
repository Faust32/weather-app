package ru.faust.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.faust.aspect.CheckSession;
import ru.faust.dto.GeocodingAPIResponseDTO;
import ru.faust.service.GeocodingAPIService;
import ru.faust.service.UserLocationsService;

@Controller
@RequestMapping("/home")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final UserLocationsService userLocationsService;

    private final GeocodingAPIService locationFinderService;

    public SearchController(UserLocationsService userLocationsService, GeocodingAPIService locationFinderService) {
        this.userLocationsService = userLocationsService;
        this.locationFinderService = locationFinderService;
    }

    @CheckSession
    @GetMapping("/search")
    public String searchForLocation(@RequestParam("name") String locationName, Model model) {
        logger.info("Search for location {}", locationName);
        model.addAttribute("searchedLocations", locationFinderService.getLocations(locationName));
        logger.info("Search for location {} was succeeded.", locationName);
        return "search-results";
    }

    @CheckSession
    @PostMapping("/add-location")
    public String addLocation(@ModelAttribute GeocodingAPIResponseDTO geocodingAPIResponseDTO,
                              @RequestParam(value = "searchName", required = false) String searchName,
                              RedirectAttributes redirectAttributes) {
        logger.info("Adding location: {}", geocodingAPIResponseDTO.toString());
        userLocationsService.save(geocodingAPIResponseDTO);
        logger.info("Adding location {} was succeeded.", geocodingAPIResponseDTO);
        redirectAttributes.addAttribute("name", searchName);
        return "redirect:/home/search";
    }
}
