package com.wolfsnetz.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SiteModelAdvice
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SiteModelAdvice.class);

    private final String displayName;

    public SiteModelAdvice(Environment environment)
    {
        String environmentDisplayName = environment.getProperty("APP_DISPLAY_NAME");
        String propertyDisplayName = environment.getProperty("app.display-name");

        this.displayName = firstNonBlank(environmentDisplayName, propertyDisplayName, "Webserver");

        LOGGER.info("Using site display name: {}", this.displayName);
    }

    @ModelAttribute("siteName")
    public String siteName()
    {
        return displayName;
    }

    private String firstNonBlank(String... values)
    {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "";
    }
}
