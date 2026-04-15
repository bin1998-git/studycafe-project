package com.tenco.seat_usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


import java.time.LocalDateTime;

import java.time.LocalDateTime;

import java.time.LocalDateTime;
@Data
@Builder
public class SeatUsageDTO {
    private int usageId;
    private int memberId;
    private int seatId;
    private int memberTicketId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
