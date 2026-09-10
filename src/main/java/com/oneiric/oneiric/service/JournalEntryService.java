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
    
    private static final java.util.regex.Pattern IMAGE_PATTERN =
    	    java.util.regex.Pattern.compile("!\\[[^\\]]*\\]\\(([^)]+)\\)");

    	public String extractFirstImageUrl(String content) {
    	    if (content == null) return null;
    	    var matcher = IMAGE_PATTERN.matcher(content);
    	    if (matcher.find()) {
    	        return matcher.group(1);
    	    }
    	    return null;
    	}

    	public String stripImages(String content) {
    	    if (content == null) return "";
    	    return IMAGE_PATTERN.matcher(content).replaceAll("").trim();
    	}
    	// ── Current writing streak (consecutive days with at least one entry, ending today or yesterday) ──
    	public int getCurrentStreak(User user) {
    	    List<JournalEntry> entries = journalEntryRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    	    if (entries.isEmpty()) return 0;

    	    // Collect distinct calendar dates the user wrote on
    	    java.util.Set<java.time.LocalDate> writeDates = new java.util.TreeSet<>(java.util.Collections.reverseOrder());
    	    for (JournalEntry e : entries) {
    	        writeDates.add(e.getCreatedAt().toLocalDate());
    	    }

    	    java.time.LocalDate today = java.time.LocalDate.now();
    	    java.time.LocalDate cursor = today;

    	    // Streak only counts if the most recent entry was today or yesterday
    	    if (!writeDates.contains(today) && !writeDates.contains(today.minusDays(1))) {
    	        return 0;
    	    }

    	    // If nothing written today yet, start counting from yesterday
    	    if (!writeDates.contains(cursor)) {
    	        cursor = cursor.minusDays(1);
    	    }

    	    int streak = 0;
    	    while (writeDates.contains(cursor)) {
    	        streak++;
    	        cursor = cursor.minusDays(1);
    	    }
    	    return streak;
    	}

    	// ── Most frequently used mood among active entries ──
    	public String getMostUsedMood(User user) {
    	    List<JournalEntry> entries = journalEntryRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    	    java.util.Map<String, Integer> moodCounts = new java.util.HashMap<>();

    	    for (JournalEntry e : entries) {
    	        if (e.getMood() != null && !e.getMood().isBlank()) {
    	            moodCounts.merge(e.getMood(), 1, Integer::sum);
    	        }
    	    }

    	    return moodCounts.entrySet().stream()
    	        .max(java.util.Map.Entry.comparingByValue())
    	        .map(java.util.Map.Entry::getKey)
    	        .orElse(null);
    	}
    	
}