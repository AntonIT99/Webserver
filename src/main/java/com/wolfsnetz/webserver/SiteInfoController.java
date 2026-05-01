package com.wolfsnetz.webserver;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiteInfoController
{
    private final SiteModelAdvice siteModelAdvice;

    public SiteInfoController(SiteModelAdvice siteModelAdvice)
    {
        this.siteModelAdvice = siteModelAdvice;
    }

    @GetMapping("/api/site/info")
    public Map<String, String> siteInfo()
    {
        return Map.of("displayName", siteModelAdvice.siteName());
    }
}
