package com.tenco.seat;

import com.tenco.member.MemberDAO;
import com.tenco.member.MemberDTO;
import com.tenco.member_ticket.MemberTicketDAO;
import com.tenco.member_ticket.MemberTicketDTO;
import com.tenco.notification.NotificationDAO;
import com.tenco.notification.NotificationDTO;
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
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final MemberDAO memberDAO = new MemberDAO();

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

    // 좌석 번호(A1, P3 등) → seatId 로 변환. 없으면 -1
    public int findSeatIdByNumber(String seatNumber) throws SQLException {
        SeatDTO s = seatDAO.findSeatByNumber(seatNumber);
        return (s == null) ? -1 : s.getSeatId();
    }

    /**
     * 사용자가 좌석 ID 숫자("1","2")로 입력했거나
     * 좌석 번호 문자열("A1","P3", "a1")로 입력했을 때
     * 모두 seatId(int) 로 변환해준다.
     * 찾지 못하면 -1.
     */
    public int resolveSeatId(String input) throws SQLException {
        if (input == null) return -1;
        String v = input.trim();
        if (v.isEmpty()) return -1;
        // 숫자면 먼저 ID로 시도
        try {
            int id = Integer.parseInt(v);
            SeatDTO byId = seatDAO.findSeatById(id);
            if (byId != null) return byId.getSeatId();
        } catch (NumberFormatException ignored) {}
        // 그래도 못 찾으면 seat_number 로 조회
        SeatDTO s = seatDAO.findSeatByNumber(v);
        return (s == null) ? -1 : s.getSeatId();
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

            // 6. 이력 알림 기록
            logNotification(memberId, "SEAT_START",
                    lookupMemberName(memberId) + " 회원이 " + seat.getSeatNumber() + " 좌석에 입실했습니다.");
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

            // 이력 알림 기록
            SeatDTO seatInfo = seatDAO.findSeatById(usage.getSeatId());
            String seatNumber = (seatInfo != null) ? seatInfo.getSeatNumber() : ("ID" + usage.getSeatId());
            logNotification(memberId, "SEAT_END",
                    lookupMemberName(memberId) + " 회원이 " + seatNumber + " 좌석에서 퇴실했습니다.");
            return true;
        }
        return false;
    }

    /** 회원명 조회 (실패 시 member#id 로 대체) */
    private String lookupMemberName(int memberId) {
        try {
            MemberDTO m = memberDAO.findById(memberId);
            return m != null && m.getName() != null ? m.getName() : ("회원#" + memberId);
        } catch (Exception e) {
            return "회원#" + memberId;
        }
    }

    /** 입/퇴실 이력 알림 저장 (실패해도 본 흐름에는 영향 없도록 swallow) */
    private void logNotification(int memberId, String type, String message) {
        try {
            NotificationDTO n = NotificationDTO.builder()
                    .memberId(memberId)
                    .type(type)
                    .message(message)
                    .isRead(false)
                    .build();
            notificationDAO.insert(n);
        } catch (Exception ignored) {}
    }
}