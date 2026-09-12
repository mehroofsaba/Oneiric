package com.oneiric.oneiric.repository;

import com.oneiric.oneiric.model.JournalEntry;
import com.oneiric.oneiric.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
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

    // Combined filter: text query, tag, and date range — any/all can be null to skip that filter
    @Query("SELECT e FROM JournalEntry e WHERE e.user = :user AND e.deletedAt IS NULL " +
           "AND (:query IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:tag IS NULL OR LOWER(e.tags) LIKE LOWER(CONCAT('%', :tag, '%'))) " +
           "AND (:startDate IS NULL OR e.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR e.createdAt <= :endDate) " +
           "ORDER BY e.createdAt DESC")
    List<JournalEntry> filterEntries(@Param("user") User user,
                                      @Param("query") String query,
                                      @Param("tag") String tag,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // Distinct tags across a user's active entries, for building a tag filter dropdown
    @Query("SELECT e.tags FROM JournalEntry e WHERE e.user = :user AND e.deletedAt IS NULL AND e.tags IS NOT NULL AND e.tags <> ''")
    List<String> findAllTagStringsForUser(@Param("user") User user);
}