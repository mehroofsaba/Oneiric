package com.oneiric.oneiric.service;

import com.oneiric.oneiric.model.JournalEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    public int calculateStreak(List<JournalEntry> entries) {
        if (entries == null || entries.isEmpty()) return 0;

        Set<LocalDate> entryDates = entries.stream()
            .filter(e -> e.getDeletedAt() == null)
            .map(e -> e.getCreatedAt().toLocalDate())
            .collect(Collectors.toSet());

        if (entryDates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate cursor = entryDates.contains(today) ? today : today.minusDays(1);

        if (!entryDates.contains(cursor)) return 0;

        int streak = 0;
        while (entryDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    public String calculateMostUsedMood(List<JournalEntry> entries) {
        if (entries == null || entries.isEmpty()) return null;

        Map<String, Long> moodCounts = entries.stream()
            .filter(e -> e.getDeletedAt() == null && e.getMood() != null)
            .collect(Collectors.groupingBy(JournalEntry::getMood, Collectors.counting()));

        return moodCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public long countActiveEntries(List<JournalEntry> entries) {
        if (entries == null) return 0;
        return entries.stream()
            .filter(e -> e.getDeletedAt() == null)
            .count();
    }
}