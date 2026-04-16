package com.tenco.seat;

import com.tenco.member_ticket.MemberTicketDAO;
import com.tenco.member_ticket.MemberTicketDTO;
import com.tenco.seat_usage.SeatUsageDAO;
import com.tenco.seat_usage.SeatUsageDTO;
import com.tenco.ticket.TicketDAO;
import com.tenco.ticket.TicketDTO;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class SeatService {

    private final SeatDAO seatDAO = new SeatDAO();
    private final SeatUsageDAO seatUsageDAO = new SeatUsageDAO();
    private final MemberTicketDAO memberTicketDAO = new MemberTicketDAO();
    private final TicketDAO ticketDAO = new TicketDAO();

    // 좌석 등록
    public boolean addSeat(SeatDTO seatDTO) throws SQLException {
        if (seatDTO.getSeatNumber() == null) {
            throw new SQLException("좌석 번호는 필수 사항 입니다!");
        }
        if (seatDTO.getSeatType() == null) {
            throw new SQLException("좌석의 종류(시간권 / 기간권)는 필수 사항 입니다!");
        }

        seatDAO.insert(seatDTO);
        return true;
    }

    // 전체 좌석 리스트 조회
    public List<SeatDTO> getSeatList() throws SQLException {
        return seatDAO.findAll();
    }

    // 사용 가능 자리 리스트 조회
    public List<SeatDTO> getAvailableSeats() throws SQLException {
        return seatDAO.findAvailable();
    }

    // 좌석 상태 변경
    public boolean modifySeatStatus(int seatId, Status status) throws SQLException {
        if (status == null) {
            throw new SQLException("좌석 상태는 필수 사항 입니다.");
        }
        if (seatId <= 0 ) {
            throw new SQLException("seatId는 0이거나 음수일 수 없습니다.");
        }

        return seatDAO.updateStatus(seatId, status);
    }

    // 좌석 제거
    public boolean removeSeat(int seatId) throws SQLException {
        if (seatId <= 0) {
            throw new SQLException("seatId 가 틀렸습니다.");
        }

        return seatDAO.delete(seatId);
    }

    public boolean checkIn(int memberId, int seatId) throws SQLException {
        // 1. 좌석 정보 및 상태 확인
        SeatDTO seat = seatDAO.findSeatById(seatId);
        if (seat == null || seat.getStatus() != Status.AVAILABLE) {
            throw new SQLException("현재 이용할 수 없는 좌석입니다.");
        }

        // 2. 보유 이용권 검증 (가장 오래된 UNUSED 또는 ACTIVE 티켓)
        MemberTicketDTO ticket = memberTicketDAO.findActiveByMemberId(memberId);
        if (ticket == null) {
            throw new SQLException("사용 가능한 이용권이 없습니다.");
        }

        // 3. 타입 일치 여부 검증 (일반-시간 / 프리미엄-기간)
        TicketDTO ticketInfo = ticketDAO.findById(ticket.getTicketId());
        boolean isTimeTicket = ticketInfo.getType() == com.tenco.ticket.TicketType.TIME;
        boolean isPeriodTicket = ticketInfo.getType() == com.tenco.ticket.TicketType.PERIOD;
        boolean isStandardSeat = seat.getSeatType() == SeatType.STANDARD;
        boolean isPremiumSeat = seat.getSeatType() == SeatType.PREMIUM;

        if ((isStandardSeat && !isTimeTicket) || (isPremiumSeat && !isPeriodTicket)) {
            throw new SQLException("좌석 타입과 보유하신 이용권 타입이 맞지 않습니다.");
        }

        // 4. 이용 내역 생성 (새로 만드신 Builder 패턴 적용)
        SeatUsageDTO usage = SeatUsageDTO.builder()
                .memberId(memberId)
                .seatId(seatId)
                .memberTicketId(ticket.getMemberTicketId())
                .startedAt(LocalDateTime.now())
                .build();

        // 5. 입실 흐름 실행 (DAO 순차 호출)
        boolean isInserted = seatUsageDAO.insert(usage);
        if (isInserted) {
            seatDAO.updateStatus(seatId, Status.IN_USE);

            // 처음 사용하는 티켓이면 활성화 및 남은 시간 설정
            if ("UNUSED".equals(ticket.getStatus())) {
                memberTicketDAO.updateStatus(ticket.getMemberTicketId(), "ACTIVE");
                memberTicketDAO.updateStartedAt(ticket.getMemberTicketId(), LocalDateTime.now());
                if (isTimeTicket) {
                    memberTicketDAO.updateRemainingMinutes(ticket.getMemberTicketId(), ticketInfo.getDurationValue());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 퇴실 처리 (SFR-026)
     * - 최신 DAO 코드에 맞추어 memberId 기반으로 활성화된 내역을 찾아 처리합니다.
     */
    public boolean checkOut(int memberId) throws SQLException {
        // 1. 현재 해당 회원이 이용 중인 내역 찾기
        SeatUsageDTO usage = seatUsageDAO.findActiveByMemberId(memberId);
        if (usage == null) {
            throw new SQLException("현재 이용 중인 좌석이 없습니다.");
        }

        // 2. 이용권 정보 조회
        MemberTicketDTO ticket = memberTicketDAO.findById(usage.getMemberTicketId());
        TicketDTO ticketInfo = ticketDAO.findById(ticket.getTicketId());

        // 3. 남은 시간 계산 및 차감 (시간권인 경우)
        if (ticketInfo.getType() == com.tenco.ticket.TicketType.TIME) {
            long usedMinutes = Duration.between(usage.getStartedAt(), LocalDateTime.now()).toMinutes();

            int updatedRemaining = ticket.getRemainingMinutes() - (int) usedMinutes;
            if (updatedRemaining <= 0) {
                updatedRemaining = 0;
                memberTicketDAO.updateStatus(ticket.getMemberTicketId(), "EXPIRED");
            }
            memberTicketDAO.updateRemainingMinutes(ticket.getMemberTicketId(), updatedRemaining);
        }

        // 4. 퇴실 흐름 실행
        // (작성하신 SeatUsageDAO의 updateEndedAt이 memberId를 받도록 되어 있어 그에 맞췄습니다)
        boolean isUsageUpdated = seatUsageDAO.updateEndedAt(memberId);

        if (isUsageUpdated) {
            seatDAO.updateStatus(usage.getSeatId(), Status.AVAILABLE);
            return true;
        }
        return false;
    }
}