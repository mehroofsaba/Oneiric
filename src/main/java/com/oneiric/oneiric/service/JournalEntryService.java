package com.oneiric.oneiric.service;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import com.oneiric.oneiric.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

    // ── Updated: createEntry now accepts tags ──
    public JournalEntry createEntry(String title, String content, String mood, String tags, User user) {
        JournalEntry entry = new JournalEntry();
        entry.setTitle(title);
        entry.setContent(content);
        entry.setMood(mood);
        entry.setTags(tags);
        entry.setUser(user);
        return journalEntryRepository.save(entry);
    }

    public Optional<JournalEntry> getEntryById(Long id) {
        return journalEntryRepository.findById(id);
    }

    // ── Updated: updateEntry now accepts mood + tags too ──
    public JournalEntry updateEntry(JournalEntry entry, String title, String content, String mood, String tags) {
        entry.setTitle(title);
        entry.setContent(content);
        entry.setMood(mood);
        entry.setTags(tags);
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

    // ── New: combined filter for search + tag + date range ──
    public List<JournalEntry> filterEntries(User user, String query, String tag, LocalDate startDate, LocalDate endDate) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String cleanTag = (tag != null && !tag.trim().isEmpty()) ? tag.trim() : null;
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? LocalDateTime.of(endDate, LocalTime.MAX) : null;

        return journalEntryRepository.filterEntries(user, cleanQuery, cleanTag, startDateTime, endDateTime);
    }

    // ── New: distinct list of tags the user has used, for a filter dropdown ──
    public List<String> getAllTagsForUser(User user) {
        List<String> rawTagStrings = journalEntryRepository.findAllTagStringsForUser(user);
        return rawTagStrings.stream()
                .flatMap(tagString -> java.util.Arrays.stream(tagString.split(",")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
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

        java.util.Set<LocalDate> writeDates = new TreeSet<>(java.util.Collections.reverseOrder());
        for (JournalEntry e : entries) {
            writeDates.add(e.getCreatedAt().toLocalDate());
        }

        LocalDate today = LocalDate.now();
        LocalDate cursor = today;

        if (!writeDates.contains(today) && !writeDates.contains(today.minusDays(1))) {
            return 0;
        }

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