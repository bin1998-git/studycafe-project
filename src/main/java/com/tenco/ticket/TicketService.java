package com.tenco.ticket;

import com.tenco.member_ticket.MemberTicketDAO;
import com.tenco.member_ticket.MemberTicketDTO;

import java.sql.SQLException;
import java.util.List;

public class TicketService {
    private final TicketDAO ticketDAO = new TicketDAO();
    private final MemberTicketDAO memberTicketDAO = new MemberTicketDAO();

    // 이용권 상품 등록
    public boolean addTicket(TicketDTO ticketDTO) throws SQLException {
        return ticketDAO.insert(ticketDTO);
    }

    // 전체 이용권 상품 반환
    public List<TicketDTO> getTicketList() throws SQLException {
        return ticketDAO.findAll();
    }

    // ID로 조회, 없으면 예외
    public TicketDTO getTicketById(int ticketId) throws SQLException {
        if (ticketId <= 0) {
            throw new SQLException("유효하지 않은 ticketId 입니다.");
        }

        TicketDTO ticketDTO = ticketDAO.findById(ticketId);
        if (ticketDTO == null) {
            throw new SQLException("해당 이용권이 존재하지 않습니다.");
        }

        return ticketDTO;
    }

    // 이용권 상품 수정
    public boolean modifyTicket(TicketDTO ticketDTO) throws SQLException {
        return ticketDAO.update(ticketDTO);
    }

    // 이용권 상품 삭제
    public boolean removeTicket(int ticketId) throws SQLException {
        return ticketDAO.delete(ticketId);
    }

    // 회원에게 이용권 발급
    public boolean issueTicketToMember(int memberId, int ticketId) throws SQLException {
        MemberTicketDTO memberTicketDTO = MemberTicketDTO.builder()
                .memberId(memberId)
                .ticketId(ticketId)
                .startedAt(null)
                .expiredAt(null)
                .status("ACTIVE")
                .build();

        return memberTicketDAO.insert(memberTicketDTO);
    }

    // 회원 보유 이용권 목록 조회
    public List<MemberTicketDTO> getMemberTickets(int memberId) throws SQLException {
        return memberTicketDAO.findByMemberId(memberId);
    }
}