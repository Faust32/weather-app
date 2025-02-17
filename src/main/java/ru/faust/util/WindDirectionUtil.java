package ru.faust.util;

import org.springframework.stereotype.Component;

@Component
public class WindDirectionUtil {

    public String getWindDirection(int degrees) {
        String[] directions = {
                "North", "North-Northeast", "Northeast", "East-Northeast", "East", "East-Southeast", "Southeast", "South-Southeast",
                "South", "South-Southwest", "Southwest", "West-Southwest", "West", "West-Northwest", "Northwest", "North-Northwest", "North"
        };

        int index = (int) Math.round(degrees / 22.5) % 16;
        return directions[index];
    }
}
