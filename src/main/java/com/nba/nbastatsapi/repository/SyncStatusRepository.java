package com.nba.nbastatsapi.repository;

import com.nba.nbastatsapi.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncStatusRepository
        extends JpaRepository<SyncStatus, String> {
}