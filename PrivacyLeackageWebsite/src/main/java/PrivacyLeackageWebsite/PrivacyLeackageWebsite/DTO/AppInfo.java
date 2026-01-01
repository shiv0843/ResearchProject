// src/main/java/PrivacyLeackageWebsite/PrivacyLeackageWebsite/DTO/AppInfo.java
package PrivacyLeackageWebsite.PrivacyLeackageWebsite.DTO;

import java.util.List;

public class AppInfo {
    private String appName;
    private String packageName;
    private List<String> permissions;
    private List<String> trackers;

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    public List<String> getTrackers() { return trackers; }
    public void setTrackers(List<String> trackers) { this.trackers = trackers; }
}


//Comments
