package com.oneiric.oneiric.controller;

import com.oneiric.oneiric.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

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
            Model model) {

        var userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with that username.");
            return "login";
        }

        var user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Incorrect password.");
            return "login";
        }

        return "redirect:/chamber";
    }
    @GetMapping("/chamber")
    public String showChamber() {
        return "chamber";
    }

}