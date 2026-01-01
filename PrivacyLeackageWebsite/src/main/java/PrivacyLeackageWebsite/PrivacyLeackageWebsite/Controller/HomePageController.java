package PrivacyLeackageWebsite.PrivacyLeackageWebsite.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomePageController {

    // Serve the homepage
    @GetMapping("/home")
    public String home() {
        return "index";  // Thymeleaf template name (index.html)
    }

    // Optional about page
    @GetMapping("/about")
    public String about() {
        return "about";  // Create about.html in templates
    }

    // Optional contact page
    @GetMapping("/contact")
    public String contact() {
        return "contact"; // Create contact.html in templates
    }

    // Optional research page
    @GetMapping("/research")
    public String research() {
        return "research"; // Create research.html in templates
    }
}
