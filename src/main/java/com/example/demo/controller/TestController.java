package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Value("${maven.path:mvn}")
    private String mavenPath;
    
    @Value("${test.project.path:/app/backend-test-code}")
    private String testProjectPath;
    
    private static class TestFailure {
        String testName;
        String message;
        String stackTrace;
        String lineNumber;
        double timeElapsed;
        
        TestFailure(String testName, double timeElapsed) {
            this.testName = testName;
            this.timeElapsed = timeElapsed;
            this.message = "";
            this.stackTrace = "";
            this.lineNumber = "";
        }
    }
    
    @GetMapping("/")
    public String runTest() {
        logger.info("Running tests directly from root URL");
        
        StringBuilder output = new StringBuilder();
        output.append("<!DOCTYPE html><html><head>");
        output.append("<style>");
        output.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }");
        output.append(".card { background: white; border-radius: 8px; padding: 20px; margin: 10px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        output.append(".success { color: #28a745; }");
        output.append(".failure { color: #dc3545; }");
        output.append(".warning { color: #ffc107; }");
        output.append(".test-summary { font-size: 1.2em; margin: 15px 0; padding: 15px; background: #f8f9fa; border-radius: 5px; }");
        output.append(".error-details { background: #fff3f3; padding: 15px; border-left: 4px solid #dc3545; margin: 10px 0; }");
        output.append(".details-hidden { display: none; }");
        output.append(".show-more-btn { background: #007bff; color: white; border: none; padding: 10px 20px; ");
        output.append("border-radius: 5px; cursor: pointer; margin: 10px 0; font-size: 14px; }");
        output.append(".show-more-btn:hover { background: #0056b3; }");
        output.append("</style>");
        output.append("<script>");
        output.append("function toggleDetails() {");
        output.append("  var details = document.getElementById('additional-details');");
        output.append("  var btn = document.getElementById('show-more-btn');");
        output.append("  if (details.classList.contains('details-hidden')) {");
        output.append("    details.classList.remove('details-hidden');");
        output.append("    btn.textContent = 'Show Less';");
        output.append("  } else {");
        output.append("    details.classList.add('details-hidden');");
        output.append("    btn.textContent = 'Show More';");
        output.append("  }");
        output.append("}");
        output.append("</script>");
        output.append("</head><body>");
        
        StringBuilder mainContent = new StringBuilder();
        StringBuilder additionalContent = new StringBuilder();
        List<TestFailure> failures = new ArrayList<>();
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(mavenPath, "clean", "install");
            processBuilder.directory(new java.io.File(testProjectPath));
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            boolean isInTestSection = false;
            TestFailure currentFailure = null;
            boolean isCapturingStackTrace = false;
            int totalTests = 0, totalFailures = 0, totalErrors = 0, totalSkipped = 0;
            
            while ((line = reader.readLine()) != null) {
                // Clean the line from [INFO] or [ERROR] prefix
                String cleanLine = line.replaceAll("\\[(INFO|ERROR)\\]\\s*", "").trim();
                
                // Capture test failure
                if (cleanLine.contains("<<< FAILURE!")) {
                    Pattern pattern = Pattern.compile("(.*?)Time elapsed: ([\\d.]+) s.*");
                    Matcher matcher = pattern.matcher(cleanLine);
                    if (matcher.find()) {
                        String testName = matcher.group(1).trim();
                        double timeElapsed = Double.parseDouble(matcher.group(2));
                        currentFailure = new TestFailure(testName, timeElapsed);
                        isCapturingStackTrace = true;
                    }
                    continue;
                }
                
                // Capture test results
                if (cleanLine.contains("Tests run:")) {
                    Pattern pattern = Pattern.compile("Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+)");
                    Matcher matcher = pattern.matcher(cleanLine);
                    if (matcher.find()) {
                        totalTests = Integer.parseInt(matcher.group(1));
                        totalFailures = Integer.parseInt(matcher.group(2));
                        totalErrors = Integer.parseInt(matcher.group(3));
                        totalSkipped = Integer.parseInt(matcher.group(4));
                        // If this is a final summary (contains "Results:"), update the counts
                        if (line.contains("Results:")) {
                            break; // Stop processing after final results
                        }
                    }
                    continue;
                }
                
                // Capture line number from failure summary
                if (cleanLine.contains(".java:")) {
                    Pattern pattern = Pattern.compile("(.*?\\.java):(\\d+)(.*)");
                    Matcher matcher = pattern.matcher(cleanLine);
                    if (matcher.find() && currentFailure != null) {
                        currentFailure.lineNumber = matcher.group(2);
                    }
                }
                
                // Capture failure message and stack trace
                if (currentFailure != null && isCapturingStackTrace) {
                    if (cleanLine.startsWith("at ")) {
                        currentFailure.stackTrace += cleanLine + "<br>";
                    } else if (!cleanLine.isEmpty() && !cleanLine.contains("Tests run:")) {
                        if (currentFailure.message.isEmpty()) {
                            currentFailure.message = cleanLine;
                        }
                        if (cleanLine.contains("expected")) {
                            failures.add(currentFailure);
                            isCapturingStackTrace = false;
                            currentFailure = null;
                        }
                    }
                }
                
                // Add to additional content
                if (!line.trim().isEmpty()) {
                    additionalContent.append(line).append("<br>");
                }
            }
            
            process.waitFor();
            
            // Add test execution header
            mainContent.append("<div class='card'><h3>🧪 Test Execution Results</h3>");
            
            // Add test summary
            mainContent.append("<div class='test-summary'>");
            mainContent.append("📊 Test Summary:<br>");
            mainContent.append("✓ Total Tests: ").append(totalTests).append("<br>");
            if (totalFailures > 0) mainContent.append("❌ Failures: ").append(totalFailures).append("<br>");
            if (totalErrors > 0) mainContent.append("⚠️ Errors: ").append(totalErrors).append("<br>");
            if (totalSkipped > 0) mainContent.append("⏭️ Skipped: ").append(totalSkipped).append("<br>");
            
            if (totalFailures == 0 && totalErrors == 0 && totalSkipped == 0) {
                mainContent.append("<div class='success'>✅ All tests passed successfully!</div>");
            }
            mainContent.append("</div>");
            
            // Add failure details
            if (!failures.isEmpty()) {
                mainContent.append("<h4>❌ Test Failures:</h4>");
                for (TestFailure failure : failures) {
                    mainContent.append("<div class='error-details'>");
                    mainContent.append("<strong>Failed Test:</strong> ").append(failure.testName).append("<br>");
                    mainContent.append("<strong>Time Elapsed:</strong> ").append(failure.timeElapsed).append(" seconds<br>");
                    if (!failure.lineNumber.isEmpty()) {
                        mainContent.append("<strong>Line Number:</strong> ").append(failure.lineNumber).append("<br>");
                    }
                    mainContent.append("<strong>Error Message:</strong> ").append(failure.message).append("<br>");
                    if (!failure.stackTrace.isEmpty()) {
                        mainContent.append("<strong>Stack Trace:</strong><br>");
                        mainContent.append("<pre>").append(failure.stackTrace).append("</pre>");
                    }
                    mainContent.append("</div>");
                }
            }
            
            mainContent.append("</div>");
            
            // Add main content
            output.append(mainContent);
            
            // Add the show more button and additional content
            output.append("<button id='show-more-btn' class='show-more-btn' onclick='toggleDetails()'>Show More</button>");
            output.append("<div id='additional-details' class='details-hidden card'>");
            output.append("<h4>Build Process Details:</h4>");
            output.append(additionalContent);
            output.append("</div>");
            
        } catch (Exception e) {
            output.append("<div class='card failure'>");
            output.append("<h3>❌ Error executing tests:</h3>");
            output.append("<p>").append(e.getMessage()).append("</p>");
            output.append("</div>");
        }
        
        output.append("</body></html>");
        return output.toString();
    }
}
