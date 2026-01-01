// src/main/java/PrivacyLeackageWebsite/PrivacyLeackageWebsite/Service/AppAnalysisService.java
package PrivacyLeackageWebsite.PrivacyLeackageWebsite.Service;

import PrivacyLeackageWebsite.PrivacyLeackageWebsite.Controller.AppAnalysisResult;
import PrivacyLeackageWebsite.PrivacyLeackageWebsite.DTO.AppInfo;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppAnalysisService {

    private static final Map<String, String> FRIENDLY = new HashMap<>();
    private static final Map<String, Integer> WEIGHT = new HashMap<>();
    private static final Set<String> KNOWN_TRACKERS = new HashSet<>();

    static {
        FRIENDLY.put("ANDROID.PERMISSION.CAMERA", "Camera");
        FRIENDLY.put("ANDROID.PERMISSION.RECORD_AUDIO", "Microphone");
        FRIENDLY.put("ANDROID.PERMISSION.READ_CONTACTS", "Contacts");
        FRIENDLY.put("ANDROID.PERMISSION.ACCESS_FINE_LOCATION", "Location (fine)");
        FRIENDLY.put("ANDROID.PERMISSION.ACCESS_COARSE_LOCATION", "Location (coarse)");
        FRIENDLY.put("ANDROID.PERMISSION.WRITE_EXTERNAL_STORAGE", "Write storage");
        FRIENDLY.put("ANDROID.PERMISSION.READ_EXTERNAL_STORAGE", "Read storage");
        FRIENDLY.put("ANDROID.PERMISSION.SEND_SMS", "Send SMS");
        FRIENDLY.put("ANDROID.PERMISSION.RECEIVE_SMS", "Receive SMS");
        FRIENDLY.put("ANDROID.PERMISSION.INTERNET", "Network");
        FRIENDLY.put("INTERNET", "Network");

        WEIGHT.put("ANDROID.PERMISSION.CAMERA", 6);
        WEIGHT.put("ANDROID.PERMISSION.RECORD_AUDIO", 6);
        WEIGHT.put("ANDROID.PERMISSION.READ_CONTACTS", 6);
        WEIGHT.put("ANDROID.PERMISSION.ACCESS_FINE_LOCATION", 6);
        WEIGHT.put("ANDROID.PERMISSION.ACCESS_COARSE_LOCATION", 5);
        WEIGHT.put("ANDROID.PERMISSION.WRITE_EXTERNAL_STORAGE", 4);
        WEIGHT.put("ANDROID.PERMISSION.READ_EXTERNAL_STORAGE", 4);
        WEIGHT.put("ANDROID.PERMISSION.SEND_SMS", 5);
        WEIGHT.put("ANDROID.PERMISSION.RECEIVE_SMS", 5);
        WEIGHT.put("ANDROID.PERMISSION.INTERNET", 1);
        WEIGHT.put("INTERNET", 1);

        KNOWN_TRACKERS.addAll(Arrays.asList("firebase", "admob", "branch", "appsflyer", "mopub", "facebook", "adjust"));
    }

    public List<AppAnalysisResult> analyzeApps(List<AppInfo> apps) {
        List<AppAnalysisResult> results = new ArrayList<>();
        for (AppInfo app : apps) {
            AppAnalysisResult r = analyzeSingleApp(app);
            results.add(r);
        }
        results.sort(Comparator.comparing(AppAnalysisResult::getRiskOrder).thenComparing(AppAnalysisResult::getScore).reversed());
        return results;
    }

    private AppAnalysisResult analyzeSingleApp(AppInfo app) {
        int score = 0;
        Map<String, Integer> permCounts = new LinkedHashMap<>();
        Set<String> notable = new LinkedHashSet<>();

        List<String> perms = Optional.ofNullable(app.getPermissions()).orElse(Collections.emptyList());
        for (String raw : perms) {
            if (raw == null || raw.isBlank()) continue;
            String up = raw.trim().toUpperCase();
            String[] pieces = up.split("[,;:]");
            if (pieces.length == 0) pieces = new String[]{up};
            for (String piece : pieces) {
                if (piece == null || piece.isBlank()) continue;
                String key = piece.trim();
                int w = WEIGHT.getOrDefault(key, 1);
                score += w;
                String friendly = FRIENDLY.getOrDefault(key, prettify(key));
                permCounts.put(friendly, permCounts.getOrDefault(friendly, 0) + 1);
                if (w >= 3) notable.add(friendly);
            }
        }

        List<String> trackersFound = new ArrayList<>();
        List<String> trackers = Optional.ofNullable(app.getTrackers()).orElse(Collections.emptyList());
        for (String t : trackers) {
            if (t == null || t.isBlank()) continue;
            String tn = t.trim().toLowerCase();
            if (KNOWN_TRACKERS.contains(tn)) {
                trackersFound.add(tn);
                score += 6;
            }
        }

        String risk;
        if (score >= 15) risk = "High";
        else if (score >= 7) risk = "Medium";
        else risk = "Low";

        String reason;
        if (notable.isEmpty() && trackersFound.isEmpty()) {
            reason = "No notable risky permissions or trackers detected.";
        } else {
            List<String> parts = new ArrayList<>();
            if (!notable.isEmpty()) parts.add("Permissions: " + String.join(", ", notable));
            if (!trackersFound.isEmpty()) parts.add("Trackers: " + String.join(", ", trackersFound));
            reason = String.join("; ", parts);
        }

        AppAnalysisResult r = new AppAnalysisResult();
        r.setAppName(Optional.ofNullable(app.getAppName()).filter(s->!s.isBlank()).orElse(app.getPackageName()));
        r.setPackageName(app.getPackageName());
        r.setScore(score);
        r.setRiskLevel(risk);
        r.setReason(reason);
        r.setTopPermissions(permCounts.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new)));
        r.setTrackers(trackersFound);
        r.setAllPermissions(new ArrayList<>(perms));
        return r;
    }

    private String prettify(String key) {
        String s = key.replace("ANDROID.PERMISSION.", "");
        s = s.replace('_', ' ').toLowerCase();
        if (s.length() == 0) return key;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
