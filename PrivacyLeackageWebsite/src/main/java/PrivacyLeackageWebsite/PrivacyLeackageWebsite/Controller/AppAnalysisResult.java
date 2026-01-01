// src/main/java/PrivacyLeackageWebsite/PrivacyLeackageWebsite/Controller/AppAnalysisResult.java
package PrivacyLeackageWebsite.PrivacyLeackageWebsite.Controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class AppAnalysisResult {
    private String appName;
    private String packageName;
    private String riskLevel;
    private String reason;
    private int score;
    private int maxScore;
    private int riskPercent;

    // topPermissions: friendly name -> aggregated value (e.g. count or weight)
    private Map<String, Integer> topPermissions = new LinkedHashMap<>();

    // full permission weight map (optional)
    private Map<String, Integer> permissionWeights;
    private Map<String, Integer> permissionPercent; // contribution %

    private List<String> trackers;
    private List<String> recommendations;
    private List<String> allPermissions;

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public int getRiskPercent() { return riskPercent; }
    public void setRiskPercent(int riskPercent) { this.riskPercent = riskPercent; }

    public Map<String, Integer> getTopPermissions() { return topPermissions; }
    public void setTopPermissions(Map<String, Integer> topPermissions) { this.topPermissions = topPermissions; }

    public Map<String, Integer> getPermissionWeights() { return permissionWeights; }
    public void setPermissionWeights(Map<String, Integer> permissionWeights) { this.permissionWeights = permissionWeights; }

    public Map<String, Integer> getPermissionPercent() { return permissionPercent; }
    public void setPermissionPercent(Map<String, Integer> permissionPercent) { this.permissionPercent = permissionPercent; }

    public List<String> getTrackers() { return trackers; }
    public void setTrackers(List<String> trackers) { this.trackers = trackers; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public List<String> getAllPermissions() { return allPermissions; }
    public void setAllPermissions(List<String> allPermissions) { this.allPermissions = allPermissions; }

    // helper for sorting results by risk
    public int getRiskOrder() {
        if ("High".equalsIgnoreCase(riskLevel)) return 3;
        if ("Medium".equalsIgnoreCase(riskLevel)) return 2;
        return 1;
    }
}
