package com.hybridrag.service.UploadService;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class UrlIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(UrlIngestionService.class);

    public Map<String, Object> testUrlScraping(String url, String selector) throws Exception {
        logger.info("Connecting to URL using Playwright: {} with selector: {}", url, selector);
        
        String rawText = "";

        // 1. Launch Headless Browser to execute JavaScript
        try (Playwright playwright = Playwright.create()) {
            // Launch a headless chromium browser
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            
            // Go to the URL
            page.navigate(url);
            
            // Wait for the specific element to appear in the DOM (this prevents 30s timeouts on sites that never stop loading ads/analytics)
            page.waitForSelector(selector);
            
            // Extract the visible text directly from the specified element
            rawText = page.innerText(selector);
        }

        // 2. Sanitize text: remove excessive whitespace, tabs, and newlines
        String cleanText = rawText.replaceAll("\\s+", " ").trim();

        // 3. Log the result so we can inspect it before adding Vector DB logic
        logger.info("================ EXTRACTED AND CLEANED TEXT ================");
        logger.info(cleanText);
        logger.info("============================================================");
        logger.info("Total characters extracted: {}", cleanText.length());

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", "URL successfully scraped with Playwright. Check console for text.");
        response.put("extractedCharacters", cleanText.length());
        
        return response;
    }
}
