package com.tenco.seat_usage;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SeatUsageDTO {
    private int usageId;
    private int memberId;
    private int seatId;
    private int memberTicketId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
