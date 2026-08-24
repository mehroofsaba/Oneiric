package com.oneiric.oneiric.service;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import com.oneiric.oneiric.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    // Returns only active (non-deleted) entries
    public List<JournalEntry> getEntriesForUser(User user) {
        return journalEntryRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    }

    // Returns only trashed entries
    public List<JournalEntry> getTrashedEntriesForUser(User user) {
        return journalEntryRepository.findByUserAndDeletedAtIsNotNullOrderByDeletedAtDesc(user);
    }

    // Returns only favorited active entries
    public List<JournalEntry> getFavoriteEntriesForUser(User user) {
        return journalEntryRepository.findByUserAndFavoriteIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    }

    public JournalEntry createEntry(String title, String content, String mood, User user) {
        JournalEntry entry = new JournalEntry();
        entry.setTitle(title);
        entry.setContent(content);
        entry.setMood(mood);
        entry.setUser(user);
        return journalEntryRepository.save(entry);
    }

    public Optional<JournalEntry> getEntryById(Long id) {
        return journalEntryRepository.findById(id);
    }

    public JournalEntry updateEntry(JournalEntry entry, String title, String content) {
        entry.setTitle(title);
        entry.setContent(content);
        return journalEntryRepository.save(entry);
    }

    // Hard delete — permanent, only called from trash page
    public void deleteEntry(JournalEntry entry) {
        journalEntryRepository.delete(entry);
    }

    // Soft delete — moves to trash
    public void softDeleteEntry(JournalEntry entry) {
        entry.setDeletedAt(LocalDateTime.now());
        journalEntryRepository.save(entry);
    }

    // Restore from trash
    public void restoreEntry(JournalEntry entry) {
        entry.setDeletedAt(null);
        journalEntryRepository.save(entry);
    }

    // Toggle favorite on/off
    public boolean toggleFavorite(JournalEntry entry) {
        entry.setFavorite(!entry.isFavorite());
        journalEntryRepository.save(entry);
        return entry.isFavorite();
    }

    public List<JournalEntry> searchEntries(User user, String query) {
        if (query == null || query.trim().isEmpty()) {
            return journalEntryRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
        }
        return journalEntryRepository.searchByUser(user, query.trim());
    }

    // Count active entries (for chamber stats)
    public long countEntriesForUser(User user) {
        return journalEntryRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user).size();
    }
}