package com.example.demo.model;

public class TestResult {
    private int totalTests;
    private int failures;
    private int errors;
    private int skipped;
    private String testName;
    private String errorMessage;
    private String executionTime;
    private boolean isSuccess;

    public TestResult() {
        this.isSuccess = true;
    }

    // Getters and setters
    public int getTotalTests() { return totalTests; }
    public void setTotalTests(int totalTests) { this.totalTests = totalTests; }
    
    public int getFailures() { return failures; }
    public void setFailures(int failures) { this.failures = failures; }
    
    public int getErrors() { return errors; }
    public void setErrors(int errors) { this.errors = errors; }
    
    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }
    
    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getExecutionTime() { return executionTime; }
    public void setExecutionTime(String executionTime) { this.executionTime = executionTime; }
    
    public boolean isSuccess() { return isSuccess; }
    public void setSuccess(boolean success) { isSuccess = success; }
}
