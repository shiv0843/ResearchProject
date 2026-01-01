package PrivacyLeackageWebsite.PrivacyLeackageWebsite.Controller;

import PrivacyLeackageWebsite.PrivacyLeackageWebsite.DTO.AppInfo;
import PrivacyLeackageWebsite.PrivacyLeackageWebsite.Service.AppAnalysisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
public class AppAnalysisController {

    private final AppAnalysisService appAnalysisService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AppAnalysisController(AppAnalysisService appAnalysisService) {
        this.appAnalysisService = appAnalysisService;
    }

    @GetMapping({"/", "/index", "/home"})
    public String home() {
        return "index";
    }

    @PostMapping("/analyze")
    public String analyzeApps(@RequestParam("file") MultipartFile file, Model model) {
        List<AppInfo> apps = new ArrayList<>();
        int skipped = 0;

        try {
            String fn = Optional.ofNullable(file.getOriginalFilename()).orElse("");
            if (fn.toLowerCase().endsWith(".json") || (file.getContentType() != null && file.getContentType().contains("json"))) {
                apps = mapper.readValue(file.getInputStream(), new TypeReference<List<AppInfo>>() {});
            } else {
                try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String[] line;
                    boolean first = true;
                    while ((line = reader.readNext()) != null) {
                        if (first) { first = false; continue; } // skip header
                        if (line.length < 2) { skipped++; continue; }
                        String appName = line[0].trim();
                        String packageName = line.length > 1 ? line[1].trim() : "";
                        String permCell = line.length > 2 ? line[2].trim() : "";
                        List<String> perms = new ArrayList<>();
                        if (!permCell.isEmpty()) {
                            String[] parts = permCell.split("[;,]");
                            for (String p : parts) if (!p.isBlank()) perms.add(p.trim());
                        }
                        if (appName.isBlank() && packageName.isBlank()) { skipped++; continue; }
                        AppInfo a = new AppInfo();
                        a.setAppName(appName.isEmpty() ? packageName : appName);
                        a.setPackageName(packageName);
                        a.setPermissions(perms);
                        apps.add(a);
                    }
                }
            }
        } catch (Exception e) {
            model.addAttribute("error", "Failed to read file: " + e.getMessage());
            // proceed with empty apps (safe defaults below)
        }

        // analyze (or return empty list if no apps)
        List<AppAnalysisResult> results = apps.isEmpty() ? Collections.emptyList() : appAnalysisService.analyzeApps(apps);

        // counts must always exist
        Map<String,Integer> counts = new LinkedHashMap<>();
        counts.put("High", 0); counts.put("Medium", 0); counts.put("Low", 0);
        for (AppAnalysisResult r : results) {
            String level = r.getRiskLevel() != null ? r.getRiskLevel() : "Low";
            counts.put(level, counts.getOrDefault(level, 0) + 1);
        }

        int total = results.size();

        // build topGlobalPerms safely (optional)
        Map<String,Integer> topGlobalPerms = new LinkedHashMap<>();
        // aggregate permission weights if AppAnalysisResult exposes permissionWeights
        for (AppAnalysisResult r : results) {
            Map<String,Integer> pw = r.getPermissionWeights();
            if (pw == null) continue;
            for (Map.Entry<String,Integer> e : pw.entrySet()) {
                topGlobalPerms.put(e.getKey(), topGlobalPerms.getOrDefault(e.getKey(), 0) + e.getValue());
            }
        }
        // keep only top 8
        List<Map.Entry<String,Integer>> sorted = new ArrayList<>(topGlobalPerms.entrySet());
        sorted.sort((a,b)->b.getValue()-a.getValue());
        Map<String,Integer> top8 = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(8, sorted.size()); ++i) top8.put(sorted.get(i).getKey(), sorted.get(i).getValue());

        model.addAttribute("results", results);
        model.addAttribute("counts", counts);
        model.addAttribute("total", total);
        model.addAttribute("topGlobalPerms", top8);
        model.addAttribute("skippedCount", skipped);
        model.addAttribute("page", "home");

        return "index";
    }

}

