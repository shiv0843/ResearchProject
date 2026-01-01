// src/main/java/PrivacyLeackageWebsite/PrivacyLeackageWebsite/Controller/AppController.java
package PrivacyLeackageWebsite.PrivacyLeackageWebsite.Controller;

import PrivacyLeackageWebsite.PrivacyLeackageWebsite.DTO.AppInfo;
import PrivacyLeackageWebsite.PrivacyLeackageWebsite.Service.AppAnalysisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/v1/apps")
@CrossOrigin(origins = "*")
public class AppController {

    @Autowired
    private AppAnalysisService appAnalysisService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeUploadedFile(@RequestParam("file") MultipartFile file) {
        try {
            List<AppInfo> apps = new ArrayList<>();
            String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
            ObjectMapper mapper = new ObjectMapper();
            if (name.endsWith(".json") || (file.getContentType()!=null && file.getContentType().contains("json"))) {
                apps = mapper.readValue(file.getInputStream(), new TypeReference<List<AppInfo>>() {});
            } else {
                try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String[] line;
                    boolean first = true;
                    while ((line = reader.readNext()) != null) {
                        if (first) { first = false; continue; }
                        if (line.length < 2) continue;
                        AppInfo a = new AppInfo();
                        a.setAppName(line[0].trim());
                        a.setPackageName(line.length > 1 ? line[1].trim() : "");
                        if (line.length > 2) {
                            String[] parts = line[2].split("[;,]");
                            List<String> perms = new ArrayList<>();
                            for(String p: parts) if (!p.isBlank()) perms.add(p.trim());
                            a.setPermissions(perms);
                        }
                        apps.add(a);
                    }
                }
            }

            List<AppAnalysisResult> results = appAnalysisService.analyzeApps(apps);
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
