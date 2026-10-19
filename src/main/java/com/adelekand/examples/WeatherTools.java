package com.adelekand.examples;

import com.adelekand.tools.annotations.AgentTool;
import com.adelekand.tools.annotations.Param;

public class WeatherTools {
    @AgentTool(description = "Get the current weather for a city")
    public String weather(
            @Param(description = "City name") String city,
            @Param(description = "metric of imperial", required = false) String units
    ) {
        return "Sunny in " + city;
    }
}
