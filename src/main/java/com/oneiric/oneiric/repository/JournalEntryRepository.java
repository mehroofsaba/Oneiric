package com.oneiric.oneiric.repository;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    List<JournalEntry> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT e FROM JournalEntry e WHERE e.user = :user AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<JournalEntry> searchByUser(@Param("user") User user, @Param("query") String query);
    
}
