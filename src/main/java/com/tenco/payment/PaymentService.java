package com.tenco.payment;

import java.time.LocalDateTime;
import java.util.List;

import com.tenco.member_ticket.MemberTicketDAO;
import com.tenco.member_ticket.MemberTicketDTO;
import com.tenco.ticket.TicketDAO;
import com.tenco.ticket.TicketDTO;
import com.tenco.ticket.TicketType;

public class PaymentService {

  private PaymentDAO paymentDAO = new PaymentDAO();
  private TicketDAO ticketDAO = new TicketDAO();
  private MemberTicketDAO memberTicketDAO = new MemberTicketDAO();

  // 결제 처리 + 이용권 발급
  public boolean processPayment(int member_id, int ticket_id, String method) {
    try {
      // 티켓 정보 조회
      TicketDTO ticket = ticketDAO.findById(ticket_id);
      if (ticket == null) {
        return false; // 티켓이 존재하지 않음
      }

      // 결제 DTO 생성 (DB ENUM 값과 대소문자 일치)
      PaymentDTO payment = PaymentDTO.builder()
          .member_id(member_id)
          .ticket_id(ticket_id)
          .amount(ticket.getPrice())
          .method(method == null ? null : method.toUpperCase())
          .status("SUCCESS")
          .paidAt(LocalDateTime.now())
          .build();

      // 결제 등록
      boolean paymentResult = paymentDAO.insert(payment);
      if (!paymentResult) {
        return false;
      }

      // 이용권 발급
      LocalDateTime expiredAt = null;
      if (ticket.getType() == TicketType.TIME) {
        expiredAt = LocalDateTime.now().plusHours(ticket.getDurationValue());
      } else if (ticket.getType() == TicketType.PERIOD) {
        expiredAt = LocalDateTime.now().plusDays(ticket.getDurationValue());
      }

      MemberTicketDTO memberTicket = MemberTicketDTO.builder()
          .memberId(member_id)
          .ticketId(ticket_id)
          .startedAt(null)
          .expiredAt(expiredAt)
          .status("ACTIVE")
          .build();

      boolean ticketResult = memberTicketDAO.insert(memberTicket);
      return ticketResult;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  // 전체 결제 내역 반환
  public List<PaymentDTO> getPaymentList() {
    try {
      return paymentDAO.findAll();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  // 회원별 결제내역 반환
  public List<PaymentDTO> getPaymentByMember(int member_id) {
    try {
      return paymentDAO.findByMemberid(member_id);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  // 환불처리 - status refund + 이용권 expired 처리
  public boolean refund(int ticket_id) {
    try {
      // ticket_id로 payment 찾기 (단순히 첫 번째 payment를 가정, 실제로는 더 정확한 로직 필요)
      List<PaymentDTO> payments = paymentDAO.findAll();
      PaymentDTO payment = null;
      for (PaymentDTO p : payments) {
        if (p.getTicket_id() == ticket_id && "SUCCESS".equalsIgnoreCase(p.getStatus())) {
          payment = p;
          break;
        }
      }
      if (payment == null) {
        return false;
      }

      // 결제 상태 변경
      boolean paymentUpdate = paymentDAO.updateStatus(payment.getPayment_id(), "REFUND");
      if (!paymentUpdate) {
        return false;
      }

      // 이용권 상태 변경 (member_ticket에서 ticket_id로 찾기)
      List<MemberTicketDTO> memberTickets = memberTicketDAO.findByMemberId(payment.getMember_id());
      for (MemberTicketDTO mt : memberTickets) {
        if (mt.getTicketId() == ticket_id && "ACTIVE".equalsIgnoreCase(mt.getStatus())) {
          boolean ticketUpdate = memberTicketDAO.updateStatus(mt.getMemberTicketId(), "EXPIRED");
          if (!ticketUpdate) {
            return false;
          }
          break;
        }
      }

      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

}
