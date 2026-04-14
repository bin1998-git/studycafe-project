package com.tenco.ticket;

import lombok.*;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {
    private int ticketId;
    private String name;
    private TicketType type;
    private int durationValue;
    private int price;
    private String description;
}