package dev.kacperjackiewicz.r2mod_sync.web;

import dev.kacperjackiewicz.r2mod_sync.service.RtzUploadService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StaticPageController {

    private final RtzUploadService rtzUploadService;

    public StaticPageController(RtzUploadService rtzUploadService) {
        this.rtzUploadService = rtzUploadService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/server.html")
    public String index(Model model) {
        model.addAttribute("processName", rtzUploadService.getProcessName());
        return "server";
    }

    @PostMapping("/server/upload")
    public String uploadRtz(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            rtzUploadService.receive(file);
            redirectAttributes.addFlashAttribute("uploadSuccess",
                    "File received. Backend processing will be enabled when the server integration is configured.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("uploadError", exception.getMessage());
        }
        return "redirect:/server.html";
    }

    @GetMapping("/login.html")
    public String loginPage() {
        return "login";
    }
}
