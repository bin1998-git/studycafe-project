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

            // todo - 1. 서비스에서 회원 유무 확인
            // todo - 2. 구매한 티켓 조회
            // todo - 3. 사용 가능 좌석 조회
            // 입석 내역 추가
            String insertSql = """
                    INSERT INTO SEAT_USAGE (member_id, seat_id, member_ticket_id, started_at, ended_at) VALUES
                    (?, ?, ?, ?, NULL)
                    """;

            try (Connection conn = DBConnectionManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

                pstmt.setInt(1, seatUsageDTO.getMemberId());
                pstmt.setInt(2, seatUsageDTO.getSeatId());
                pstmt.setInt(3, seatUsageDTO.getMemberTicketId());
                pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(seatUsageDTO.getStartedAt()));

                int rows = pstmt.executeUpdate();
                return rows > 0;
            }
    }


    // 현재 이용중인 내역 조회
    public SeatUsageDTO findActiveByMemberId(int memberId) throws SQLException {
        String sql = """
                SELECT * FROM seat_usage
                WHERE member_id = ? AND ended_at IS NULL
                ORDER BY started_at DESC
                LIMIT 1
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToUsage(rs);
                }
            }
        }

        return null;
    }


    // -- 트랜잭션
    // 퇴실 처리 ended_at 기록
    public boolean updateEndedAt(int memberId)  throws SQLException {
        String sql = """
                UPDATE seat_usage
                SET ended_at = NOW()
                WHERE member_id = ? AND ended_at IS NULL
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }



    // 전체 이용 내역 조회
    public List<SeatUsageDTO> findAll() throws SQLException {
        List<SeatUsageDTO> seatUsageDTOList = new ArrayList<>();

        String sql = """
                SELECT * FROM SEAT_USAGE ORDER BY usage_id DESC
                """;

        try (Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstm = conn.prepareStatement(sql);
            ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                seatUsageDTOList.add(mapToUsage(rs));
            }
        }
        return seatUsageDTOList;
    }


    // 회원별 이용 내역 조회
    public List<SeatUsageDTO> findByMemberId(int memberId) throws SQLException {
        List<SeatUsageDTO> seatUsageDTOList = new ArrayList<>();
        String findByMemberIdSql = """
        SELECT * FROM SEAT_USAGE WHERE member_id = ? ORDER BY started_at DESC
        """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(findByMemberIdSql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seatUsageDTOList.add(mapToUsage(rs));
                }
            }
        }
        return seatUsageDTOList;
    }



    public SeatUsageDTO mapToUsage(ResultSet rs) throws SQLException {
        return SeatUsageDTO.builder()
                .usageId(rs.getInt("usage_id"))
                .memberId(rs.getInt("member_id"))
                .seatId(rs.getInt("seat_id"))
                .memberTicketId(rs.getInt("member_ticket_id"))
                .startedAt(rs.getTimestamp("started_at").toLocalDateTime())
                .endedAt(rs.getTimestamp("ended_at") != null ? rs.getTimestamp("ended_at").toLocalDateTime() : null)
                .build();

    }


}
