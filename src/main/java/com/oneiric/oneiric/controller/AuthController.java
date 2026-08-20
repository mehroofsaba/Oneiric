package com.oneiric.oneiric.controller;

import com.oneiric.oneiric.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import com.oneiric.oneiric.service.JournalEntryService;
import com.oneiric.oneiric.model.User;
@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JournalEntryService journalEntryService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        if (userService.usernameExists(username)) {
            model.addAttribute("error", "Username is already taken.");
            return "register";
        }

        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email is already registered.");
            return "register";
        }

        userService.registerUser(username, email, password);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            Model model,
            HttpSession session) {

        var userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with that username.");
            return "login";
        }

        var user = userOpt.get();

        if (!userService.checkPassword(password, user.getPassword())) {
            model.addAttribute("error", "Incorrect password.");
            return "login";
        }

        session.setAttribute("username", user.getUsername());
        model.addAttribute("username", user.getUsername());
        return "redirect:/chamber";
    }
    @GetMapping("/chamber")
    public String showChamber(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username).get();
        long entryCount = journalEntryService.getEntriesForUser(user).size();

        model.addAttribute("username", username);
        model.addAttribute("entryCount", entryCount);
        return "chamber";
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    @GetMapping("/account")
    public String showAccount(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";
        model.addAttribute("username", username);
        return "account";
    }

    @PostMapping("/account/password")
    public String updatePassword(HttpSession session, Model model,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {

        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        model.addAttribute("username", username);

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match.");
            return "account";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "account";
        }

        boolean updated = userService.updatePassword(username, currentPassword, newPassword);
        if (!updated) {
            model.addAttribute("error", "Current password is incorrect.");
            return "account";
        }

        model.addAttribute("success", "Password updated successfully.");
        return "account";
    }

}