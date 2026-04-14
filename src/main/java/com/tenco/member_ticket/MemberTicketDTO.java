package com.tenco.member_ticket;

import com.tenco.member.MemberDTO;
import com.tenco.ticket.TicketDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberTicketDTO {
    private int memberTicketId;
    private MemberDTO memberId;
    private TicketDTO ticketId;
    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;
    private String status;
}
