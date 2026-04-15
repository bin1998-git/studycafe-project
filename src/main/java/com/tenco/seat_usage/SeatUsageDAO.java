package com.tenco.seat_usage;

import com.tenco.member.MemberDTO;
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


    // -- 트랜잭션
    // 퇴실 처리 ended_at 기록
    public boolean updateEndedAt(int memberId)  throws SQLException {
        return false;
    }



    // 전체 이용 내역 조회
    public List<SeatUsageDTO> findAll() throws SQLException {
        List<SeatUsageDTO> seatUsageDTOList = new ArrayList<>();

        String sql = """
                SELECT * FROM SEAT_USAGE WHERE usage_id
                """;

        try (Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstm = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                seatUsageDTOList.add(mapToUsage(rs));
                }
            }
        }
        return seatUsageDTOList;
    }


    // 회원별 이용 내역 조회
    public List<SeatUsageDTO> findByMemberId(int memberId) throws SQLException {
        return null;
    }



    public SeatUsageDTO mapToUsage(ResultSet rs) throws SQLException {
        return SeatUsageDTO.builder()
                .usageId(rs.getInt("usage_id "))
                .memberId(rs.getInt("member_id"))
                .seatId(rs.getInt("seat_id"))
                .memberTicketId(rs.getInt("member_ticket_id"))
                .startedAt(rs.getTimestamp("started_at").toLocalDateTime())
                .endedAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();

    }


}
