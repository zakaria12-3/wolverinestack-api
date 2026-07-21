package com.example.dto;

import java.util.List;

public class GoalSuggestionDto {

    private String bmrEstimate;
    private String tdeeEstimate;
    private List<Suggestion> goalSuggestions;
    private List<Suggestion> activitySuggestions;
    private String summary;

    public String getBmrEstimate() { return bmrEstimate; }
    public void setBmrEstimate(String bmrEstimate) { this.bmrEstimate = bmrEstimate; }

    public String getTdeeEstimate() { return tdeeEstimate; }
    public void setTdeeEstimate(String tdeeEstimate) { this.tdeeEstimate = tdeeEstimate; }

    public List<Suggestion> getGoalSuggestions() { return goalSuggestions; }
    public void setGoalSuggestions(List<Suggestion> goalSuggestions) { this.goalSuggestions = goalSuggestions; }

    public List<Suggestion> getActivitySuggestions() { return activitySuggestions; }
    public void setActivitySuggestions(List<Suggestion> activitySuggestions) { this.activitySuggestions = activitySuggestions; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static class Suggestion {
        private String value;
        private String label;
        private int confidence;
        private String reasoning;

        public Suggestion() {}

        public Suggestion(String value, String label, int confidence, String reasoning) {
            this.value = value;
            this.label = label;
            this.confidence = confidence;
            this.reasoning = reasoning;
        }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }

        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    }
}
