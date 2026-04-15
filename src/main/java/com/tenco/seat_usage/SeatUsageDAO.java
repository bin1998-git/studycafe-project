package com.tenco.seat_usage;

import com.tenco.member.MemberDTO;
import com.tenco.member_ticket.MemberTicketDTO;
import com.tenco.seat.SeatDTO;
import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SeatUsageDAO {

    // 입실 기록 생성 - 트랜잭션
    public boolean insert(SeatUsageDTO seatUsageDTO) throws SQLException {

            // todo - 1. 서비스에서 회원 유무 확인
            // todo - 2. 구매한 티켓 조회
            // todo - 3. 사용 가능 좌석 조회
            // 입석 내역 추가
            String insertSql = """
                    INSERT INTO SEAT_USAGE (member_id, seat_id, member_ticket_id, started_at, ended_at) VALUES
                    (?, ?, ?, ?, NULL)
                    """;

            // todo - 5. 좌석 상태 변경
        return false;
    }

    // 현재 이용중인 내역 조회
    public SeatUsageDTO findActiveByMemberId(int memberId) throws SQLException {
        return null;
    }

    // 퇴실 처리 ended_at 기록
    public boolean updateEndedAt(int memberId)  throws SQLException {
        return false;
    }

    // 전체 이용 내역 조회
    public List<SeatUsageDTO> findAll() throws SQLException {
        return null;
    }

    // 회원별 이용 내역 조회
    public List<SeatUsageDTO> findByMemberId(int memberId) throws SQLException {
        return null;
    }


}
