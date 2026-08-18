package com.oneiric.oneiric.repository;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByUserOrderByCreatedAtDesc(User user);

}
