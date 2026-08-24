package com.oneiric.oneiric.repository;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    // Active entries only (not soft-deleted), newest first
    List<JournalEntry> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(User user);

    // Trashed entries only
    List<JournalEntry> findByUserAndDeletedAtIsNotNullOrderByDeletedAtDesc(User user);

    // Favorites (active only)
    List<JournalEntry> findByUserAndFavoriteIsTrueAndDeletedAtIsNullOrderByCreatedAtDesc(User user);

    // Search active entries by title or content
    @Query("SELECT e FROM JournalEntry e WHERE e.user = :user AND e.deletedAt IS NULL AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<JournalEntry> searchByUser(@Param("user") User user, @Param("query") String query);
}