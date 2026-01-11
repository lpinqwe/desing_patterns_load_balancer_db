package org.example.dto;
public enum RequestState {
    CREATED,
    QUEUED,
    ASSIGNED,
    EXECUTING,
    COMPLETED,
    CANCELLED,
    TIMED_OUT
}
