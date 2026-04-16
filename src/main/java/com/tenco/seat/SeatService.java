package com.tenco.seat;

import com.tenco.member_ticket.MemberTicketDAO;
import com.tenco.member_ticket.MemberTicketDTO;
import com.tenco.seat_usage.SeatUsageDAO;
import com.tenco.seat_usage.SeatUsageDTO;
import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeatService {

    private final SeatDAO seatDAO = new SeatDAO();
    private final SeatUsageDAO seatUsageDAO = new SeatUsageDAO();
    private final MemberTicketDAO memberTicketDAO = new MemberTicketDAO();

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
        // 1. 좌석 정보 및 상태 확인 (SeatDAO)
        SeatDTO seat = seatDAO.findById(seatId);
        if (seat == null || !"AVAILABLE".equals(seat.getStatus().toString())) {
            throw new SQLException("이용 가능한 좌석이 아닙니다.");
        }

        // 2. 사용 가능한 이용권 조회 (MemberTicketDAO)
        // (참고: 회원의 UNUSED 또는 ACTIVE 상태인 티켓을 찾는 기능이 필요합니다)
        MemberTicketDTO ticket = memberTicketDAO.findActiveByMemberId(memberId);
        if (ticket == null) {
            throw new SQLException("사용 가능한 이용권이 없습니다.");
        }

        // 3. 타입 검증 (기획 문서: 일반석=시간권, 프리미엄석=기간권)
        String seatType = seat.getSeatType().toString(); // STANDARD or PREMIUM
        String ticketType = ticket.getType().toString(); // TIME or DAY

        boolean isValid = (seatType.equals("STANDARD") && ticketType.equals("TIME")) ||
                (seatType.equals("PREMIUM") && ticketType.equals("DAY"));

        if (!isValid) {
            throw new SQLException("좌석 타입과 이용권 타입이 일치하지 않습니다. (일반석-시간권 / 프리미엄석-기간권)");
        }

        // 4. 이용 내역 생성 및 저장 (SeatUsageDAO.insert 호출)
        SeatUsageDTO usage = new SeatUsageDTO();
        usage.setMemberId(memberId);
        usage.setSeatId(seatId);
        usage.setMemberTicketId(ticket.getMemberTicketId());
        usage.setStartedAt(LocalDateTime.now()); // 현재 시각 입실

        boolean isInserted = seatUsageDAO.insert(usage);

        if (isInserted) {
            // 5. 좌석 상태 변경 (SeatDAO)
            seatDAO.updateStatus(seatId, Status.valueOf("IN_USE"));

            // 6. 이용권이 미사용(UNUSED)이었다면 사용중(ACTIVE)으로 변경 및 시작일 기록
            if ("UNUSED".equals(ticket.getStatus().toString())) {
                memberTicketDAO.updateStatus(ticket.getMemberTicketId(), "ACTIVE");
                memberTicketDAO.updateStartedAt(ticket.getMemberTicketId(), LocalDateTime.now());
            }
            return true;
        }

        return false;
    }

    /**
     * 퇴실 처리 (SFR-026)
     * 제공된 SeatUsageDAO.updateEndedAt()을 활용하여 퇴실 및 시간 차감을 처리합니다.
     */
    public boolean checkOut(int usageId) throws SQLException {
        // 1. 이용 내역 상세 조회 (SeatUsageDAO에 findById(usageId)가 있다고 가정)
        // 만약 없다면 findActiveByMemberId 등을 조합해서 찾아야 합니다.
        SeatUsageDTO usage = seatUsageDAO.findById(usageId);
        if (usage == null || usage.getEndedAt() != null) {
            throw new SQLException("이미 종료되었거나 존재하지 않는 이용 내역입니다.");
        }

        // 2. 보유 이용권 정보 조회 (MemberTicketDAO)
        MemberTicketDTO ticket = memberTicketDAO.findById(usage.getMemberTicketId());

        // 3. 시간권(TIME)인 경우 남은 시간 계산 및 차감
        if ("TIME".equals(ticket.getType().toString())) {
            LocalDateTime now = LocalDateTime.now();
            // 실제 이용한 시간(분) 계산
            long usedMinutes = Duration.between(usage.getStartedAt(), now).toMinutes();

            // DTO의 남은 시간 필드(remainingMinutes)에서 차감
            int newRemaining = ticket.getRemainingMinutes() - (int) usedMinutes;

            if (newRemaining <= 0) {
                newRemaining = 0;
                memberTicketDAO.updateStatus(ticket.getMemberTicketId(), "EXPIRED"); // 만료 처리
            }
            // DB에 남은 시간 저장
            memberTicketDAO.updateRemainingMinutes(ticket.getMemberTicketId(), newRemaining);
        }

        // 4. 퇴실 시각 기록 (SeatUsageDAO.updateEndedAt 호출)
        boolean isUpdated = seatUsageDAO.updateEndedAt(usageId);

        if (isUpdated) {
            // 5. 좌석 상태 변경 (SeatDAO: AVAILABLE)
            seatDAO.updateStatus(usage.getSeatId(), Status.valueOf("AVAILABLE"));
            return true;
        }

        return false;
    }
}