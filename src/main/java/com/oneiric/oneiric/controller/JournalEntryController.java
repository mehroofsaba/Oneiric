package com.oneiric.oneiric.controller;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import com.oneiric.oneiric.service.JournalEntryService;
import com.oneiric.oneiric.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/entries")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;

    @Autowired
    private UserService userService;

    // ── List active entries ──────────────────────────────────────────────────
    @GetMapping
    public String listEntries(HttpSession session, Model model,
                              @RequestParam(required = false) String query) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username).get();

        if (query != null && !query.trim().isEmpty()) {
            model.addAttribute("entries", journalEntryService.searchEntries(user, query));
            model.addAttribute("query", query);
        } else {
            model.addAttribute("entries", journalEntryService.getEntriesForUser(user));
        }
        return "entries";
    }

    // ── New entry ────────────────────────────────────────────────────────────
    @GetMapping("/new")
    public String newEntryPage(HttpSession session) {
        if (session.getAttribute("username") == null) return "redirect:/login";
        return "entry-new";
    }

    @PostMapping("/new")
    public String createEntry(HttpSession session,
                              @RequestParam String title,
                              @RequestParam String content,
                              @RequestParam(required = false) String mood) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username).get();
        journalEntryService.createEntry(title, content, mood, user);
        return "redirect:/entries";
    }

    // ── Edit entry ───────────────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editEntryPage(@PathVariable Long id, HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<JournalEntry> entry = journalEntryService.getEntryById(id);
        if (entry.isEmpty()) return "redirect:/entries";

        model.addAttribute("entry", entry.get());
        return "entry-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateEntry(@PathVariable Long id, HttpSession session,
                              @RequestParam String title,
                              @RequestParam String content) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<JournalEntry> entry = journalEntryService.getEntryById(id);
        if (entry.isEmpty()) return "redirect:/entries";

        journalEntryService.updateEntry(entry.get(), title, content);
        return "redirect:/entries";
    }

    // ── Soft delete (move to trash) ──────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String softDelete(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<JournalEntry> entry = journalEntryService.getEntryById(id);
        if (entry.isEmpty()) return "redirect:/entries";

        // Security: only owner can delete
        if (!entry.get().getUser().getUsername().equals(username)) return "redirect:/entries";

        journalEntryService.softDeleteEntry(entry.get());
        return "redirect:/entries";
    }

    // ── Trash page ───────────────────────────────────────────────────────────
    @GetMapping("/trash")
    public String trashPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username).get();
        model.addAttribute("entries", journalEntryService.getTrashedEntriesForUser(user));
        return "trash";
    }

    // ── Restore from trash ───────────────────────────────────────────────────
    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<JournalEntry> entry = journalEntryService.getEntryById(id);
        if (entry.isEmpty()) return "redirect:/entries/trash";

        if (!entry.get().getUser().getUsername().equals(username)) return "redirect:/entries/trash";

        journalEntryService.restoreEntry(entry.get());
        return "redirect:/entries/trash";
    }

    // ── Permanent delete (from trash only) ───────────────────────────────────
    @PostMapping("/{id}/delete-permanent")
    public String permanentDelete(@PathVariable Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<JournalEntry> entry = journalEntryService.getEntryById(id);
        if (entry.isEmpty()) return "redirect:/entries/trash";

        if (!entry.get().getUser().getUsername().equals(username)) return "redirect:/entries/trash";

        journalEntryService.deleteEntry(entry.get());
        return "redirect:/entries/trash";
    }

    // ── Toggle favorite (AJAX) ───────────────────────────────────────────────
 
    @PostMapping("/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id, HttpSession session,
                                  @RequestHeader(value = "Referer", defaultValue = "/entries") String referer) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<JournalEntry> entry = journalEntryService.getEntryById(id);
        if (entry.isEmpty()) return "redirect:/entries";

        if (!entry.get().getUser().getUsername().equals(username)) return "redirect:/entries";

        journalEntryService.toggleFavorite(entry.get());
        return "redirect:" + referer;
    }
    
    // ── Favorites page ───────────────────────────────────────────────────────
    @GetMapping("/favorites")
    public String favoritesPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username).get();
        model.addAttribute("entries", journalEntryService.getFavoriteEntriesForUser(user));
        return "favorites";
    }
    
 // ── Empty entire trash ───────────────────────────────────────────────────
    @PostMapping("/trash/empty")
    public String emptyTrash(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        User user = userService.findByUsername(username).get();
        journalEntryService.getTrashedEntriesForUser(user)
            .forEach(journalEntryService::deleteEntry);

        return "redirect:/entries/trash";
    }
    
}