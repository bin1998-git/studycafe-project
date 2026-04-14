package com.tenco.seat;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class SeatDTO {
    private int seatId;
    private String seatNumber;
    private SeatType seatType; // enum
    private Status status; // enum
    private String zone;
}
