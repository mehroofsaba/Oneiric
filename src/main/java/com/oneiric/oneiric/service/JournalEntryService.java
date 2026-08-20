package com.oneiric.oneiric.service;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import com.oneiric.oneiric.repository.JournalEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    public List<JournalEntry> getEntriesForUser(User user) {
        return journalEntryRepository.findByUserOrderByCreatedAtDesc(user);
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

    public void deleteEntry(JournalEntry entry) {
        journalEntryRepository.delete(entry);
    }
}