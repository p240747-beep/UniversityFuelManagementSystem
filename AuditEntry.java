package com.university.fms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single audit trail entry.
 * Maps to FR13 - View Audit Trail.
 */
public class AuditEntry {
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String entryID;
    private String userID;
    private String userName;
    private String action;
    private String details;
    private LocalDateTime timestamp;

    public AuditEntry(String entryID, String userID, String userName,
                      String action, String details) {
        this.entryID = entryID;
        this.userID = userID;
        this.userName = userName;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public String getEntryID() { return entryID; }
    public String getUserID() { return userID; }
    public String getUserName() { return userName; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public String getTimestampStr() { return timestamp.format(FMT); }
}
