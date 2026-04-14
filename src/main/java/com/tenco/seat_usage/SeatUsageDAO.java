package com.tenco.seat_usage;

import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatUsageDAO {

    // 입실 기록 생성 - 트랜잭션
    public boolean insert(SeatUsageDTO seatUsageDTO) throws SQLException {

        String sql = """
            
            """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


        }
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
