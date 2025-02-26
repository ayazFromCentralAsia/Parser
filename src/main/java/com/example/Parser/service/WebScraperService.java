package com.example.Parser.service;

import com.example.Parser.dto.ScarpedDataResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Service;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.net.URL;

@Service
public class WebScraperService {



    public ScarpedDataResponse scrapeWebsite(String url) throws Exception {
        if (url == null || url.isEmpty()){
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        GenericContainer seleniumContainer = new GenericContainer("selenium/standalone-chrome:latest")
                .withExposedPorts(4444)
                .waitingFor(Wait.forListeningPort());
        seleniumContainer.start();

        String seleniumHost = seleniumContainer.getContainerIpAddress();
        Integer seleniumPort = seleniumContainer.getMappedPort(4444);

        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new RemoteWebDriver(
                new URL("http://" + seleniumHost + ":" + seleniumPort + "/wd/hub"), options);

        driver.get(url);

        ScarpedDataResponse scarpedDataResponse = new ScarpedDataResponse();

        String pageTitle = driver.getTitle();
        String pageDescription = driver.findElement(By.xpath("//meta[@name='description']")).getAttribute("content");
        String pageContent = driver.findElement(By.xpath("//body")).getText();
        String pageLanguage = driver.findElement(By.xpath("//html")).getAttribute("lang");
        String pageCategory = driver.findElement(By.xpath("//meta[@property='article:section']")).getAttribute("content");
        String pageLicense = driver.findElement(By.xpath("//meta[@name='license']")).getAttribute("content");

        scarpedDataResponse.setUrl(url);
        scarpedDataResponse.setTitle(pageTitle);
        scarpedDataResponse.setDescription(pageDescription);
        scarpedDataResponse.setContent(pageContent);
        scarpedDataResponse.setLanguage(pageLanguage);
        scarpedDataResponse.setCategory(pageCategory);
        scarpedDataResponse.setLicense(pageLicense);


        driver.quit();
        seleniumContainer.stop();

        return scarpedDataResponse;
    }
}
