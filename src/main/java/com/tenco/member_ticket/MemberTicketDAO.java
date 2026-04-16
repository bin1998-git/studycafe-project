package com.tenco.member_ticket;

import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MemberTicketDAO {

    private MemberTicketDTO mapToMemberTicket(ResultSet rs) throws SQLException {
        Timestamp startedTs = rs.getTimestamp("started_at");
        Timestamp expiredTs = rs.getTimestamp("expired_at");

        return MemberTicketDTO.builder()
                .memberTicketId(rs.getInt("member_ticket_id"))
                .memberId(rs.getInt("member_id"))
                .ticketId(rs.getInt("ticket_id"))
                .startedAt(startedTs != null ? startedTs.toLocalDateTime() : null)
                .expiredAt(expiredTs != null ? expiredTs.toLocalDateTime() : null)
                .status(rs.getString("status"))
                .remainingMinutes(rs.getInt("remaining_minutes"))
                .build();
    }

    // 이용권 발급 레코드 생성
    public boolean insert(MemberTicketDTO memberTicketDTO) throws SQLException {
        String sql = """
                INSERT INTO member_ticket (member_id, ticket_id, started_at, expired_at, status, remaining_minutes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberTicketDTO.getMemberId());
            pstmt.setInt(2, memberTicketDTO.getTicketId());

            if (memberTicketDTO.getStartedAt() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(memberTicketDTO.getStartedAt()));
            } else {
                pstmt.setNull(3, Types.TIMESTAMP);
            }

            if (memberTicketDTO.getExpiredAt() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(memberTicketDTO.getExpiredAt()));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }

            pstmt.setString(5, memberTicketDTO.getStatus());
            pstmt.setInt(6, memberTicketDTO.getRemainingMinutes());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    // 회원별 보유 이용권 전체 조회
    public List<MemberTicketDTO> findByMemberId(int memberId) throws SQLException {
        List<MemberTicketDTO> memberTicketList = new ArrayList<>();

        String sql = """
                SELECT *
                FROM member_ticket
                WHERE member_id = ?
                ORDER BY member_ticket_id DESC
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    memberTicketList.add(mapToMemberTicket(rs));
                }
            }
        }

        return memberTicketList;
    }

    // ACTIVE 또는 UNUSED 상태 이용권 1건 조회 (가장 오래된 것)
    public MemberTicketDTO findActiveByMemberId(int memberId) throws SQLException {
        String sql = """
                SELECT *
                FROM member_ticket
                WHERE member_id = ?
                  AND status IN ('ACTIVE', 'UNUSED')
                ORDER BY member_ticket_id ASC
                LIMIT 1
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToMemberTicket(rs);
                }
            }
        }

        return null;
    }

    // memberTicketId로 이용권 조회
    public MemberTicketDTO findById(int memberTicketId) throws SQLException {
        String sql = """
                SELECT *
                FROM member_ticket
                WHERE member_ticket_id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberTicketId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToMemberTicket(rs);
                }
            }
        }

        return null;
    }

    // 이용권 상태 변경 (권장: member_ticket_id 기준)
    public boolean updateStatus(int memberTicketId, String status) throws SQLException {
        String sql = """
                UPDATE member_ticket
                SET status = ?
                WHERE member_ticket_id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, memberTicketId);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    // 최초 사용 시각 기록
    public boolean updateStartedAt(int memberTicketId, LocalDateTime startedAt) throws SQLException {
        String sql = """
                UPDATE member_ticket
                SET started_at = ?
                WHERE member_ticket_id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startedAt));
            pstmt.setInt(2, memberTicketId);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    // 남은 시간 업데이트
    public boolean updateRemainingMinutes(int memberTicketId, int remainingMinutes) throws SQLException {
        String sql = """
                UPDATE member_ticket
                SET remaining_minutes = ?
                WHERE member_ticket_id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, remainingMinutes);
            pstmt.setInt(2, memberTicketId);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }
}