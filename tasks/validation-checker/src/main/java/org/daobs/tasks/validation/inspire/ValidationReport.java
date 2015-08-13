package org.daobs.tasks.validation.inspire;

import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validation report
 *
 * Created by francois on 08/12/14.
 */
public class ValidationReport {

    ValidationReport(double threshold) {
        this.threshold = threshold;
    }

    /**
     * The validation status.
     */
    boolean status;

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private double threshold = 100.0;

    public boolean isAboveThreshold() {
        return isAboveThreshold;
    }

    private boolean isAboveThreshold;

    /**
     * HTTP status code
     */
    private int httpStatus;

    /**
     * Validation report. Could be an exception message
     * or an XML document depending on the validator.
     */
    private String report;

    private Date startTime;
    private Date endTime;

    public double getCompletenessIndicator() {
        return completenessIndicator;
    }

    private double completenessIndicator;
    private double timeWaitingForResponseSeconds;
    private double totalTimeSeconds;
    private StopWatch watch = new StopWatch();
    
    public void setCompletenessIndicator(double completnessIndicator){
    	this.completenessIndicator = completnessIndicator;
    	this.isAboveThreshold = this.completenessIndicator >= this.threshold;
    }

    /**
     * URL of the validation report provided by the validator.
     */
    private String resultUrl;

    /**
     * Extra information provided by the validator.
     */
    private String info;

    public double getTimeWaitingForResponseSeconds() {
        return timeWaitingForResponseSeconds;
    }

    public double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public boolean getStatus() {
        return status;
    }

    public ValidationReport setStatus(boolean status) {
        this.status = status;
        return this;
    }

    public ValidationReport start() {
        this.startTime = new Date();
        watch.start();
        return this;
    }

    public ValidationReport gotResponse() {
        this.timeWaitingForResponseSeconds = watch.getTotalTimeSeconds();
        return this;
    }
    public ValidationReport stop() {
        this.endTime = new Date();
        watch.stop();
        this.totalTimeSeconds = watch.getTotalTimeSeconds();
        return this;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public ValidationReport setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
        return this;
    }
    public String getInfo() {
        return info;
    }

    public ValidationReport setInfo(String info) {
        this.info = info;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public int getHTTPStatus() {
        return httpStatus;
    }

    public ValidationReport setHTTPStatus(int httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public String getReport() {
        return report;
    }

    private final Pattern completenessPattern = Pattern.compile("CompletenessIndicator>(.*)</.*:CompletenessIndicator");

    public ValidationReport setReport(String report) {
        this.report = report;

        // Quick hack to retrive completeness indicator
        // Xpath may be better
        try {
            Matcher matcher = completenessPattern.matcher(report);
            if (matcher.find()) {
                String completeness = matcher.group(1);
                this.completenessIndicator = Double.parseDouble(completeness);
                this.isAboveThreshold = this.completenessIndicator >= this.threshold;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("Validation report:");
        buffer.append("\nInfo: ").append(this.getInfo());
        buffer.append("\nTotal time: ").append(this.getTotalTimeSeconds());
        buffer.append("\nCompleteness : ").append(this.getCompletenessIndicator());
        buffer.append("\nResult URL: ").append(this.getResultUrl());
        buffer.append("\nReport: ").append(this.getReport());
        return buffer.toString();
    }
}
