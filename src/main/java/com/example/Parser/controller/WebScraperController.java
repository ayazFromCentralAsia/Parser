package com.example.Parser.controller;

import com.example.Parser.dto.ScarpedDataResponse;
import com.example.Parser.service.WebScraperService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webScraper")
@RequiredArgsConstructor
@Tag(name = "Web Scraper", description = "Scrapes websites for data")
public class WebScraperController {

    private final WebScraperService webScraperService;

    @GetMapping("/scrape")
    public ScarpedDataResponse scrapeWebsite(@RequestParam(value = "url") String url) throws Exception {
        return webScraperService.scrapeWebsite(url);
    }

}
