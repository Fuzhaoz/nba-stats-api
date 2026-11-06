package com.nba.nbastatsapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "sync_status")
@Getter
@Setter
public class SyncStatus {

    @Id
    @Column(name = "sync_key", length = 50)
    private String syncKey;

    @Column(name = "last_synced_date", nullable = false)
    private LocalDate lastSyncedDate;
}