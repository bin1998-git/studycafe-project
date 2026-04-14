package com.tenco.member_ticket;

import com.tenco.ticket.TicketDTO;
import com.tenco.ticket.TicketType;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MemberTicketDAO {
    private MemberTicketDTO mapToMemberTicket(ResultSet rs) throws SQLException {
        return MemberTicketDTO.builder()
                .memberTicketId(rs.getInt("member_ticket_id"))
                .memberId(rs.getInt("member_id"))
                .ticketId(rs.getInt("ticket_id"))
                .startedAt(rs.getTimestamp("started_at").toLocalDateTime())
                .expiredAt(rs.getTimestamp("expired_at").toLocalDateTime())
                .build();
    }

    // 이용권 발급 레코드 생성
    public boolean insert(MemberTicketDTO memberTicketDTO) throws SQLException {
        String sql = """
                INSERT INTO member_ticket (member_id, ticket_id, started_at, expired_at, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberTicketDTO.getMemberId());
            pstmt.setInt(2, memberTicketDTO.getTicketId());
            pstmt.setTimestamp(3, Timestamp.valueOf(memberTicketDTO.getStartedAt()));
            pstmt.setTimestamp(4, Timestamp.valueOf(memberTicketDTO.getExpiredAt()));
            pstmt.setString(5, memberTicketDTO.getStatus());

            int rows = pstmt.executeUpdate();

            return rows > 0;
        }
    }

    // 회원별 보유 이용권 전체 조회
    public List<MemberTicketDTO> findByMemberId(int memberId) throws SQLException {
        List<MemberTicketDTO> memberTicketList = new ArrayList<>();
        String sql = """
                SELECT * FROM member_ticket WHERE status = 'ACTIVE'
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                memberTicketList.add(mapToMemberTicket(rs));
            }
            return memberTicketList;
        }
    }

    // ACTIVE 상태 이용권 1건 조회
    public MemberTicketDTO findActiveByMemberId(int memberId) throws SQLException {
        String sql = """
                SELECT *
                FROM member_ticket
                WHERE member_id = ?
                	AND status = 'ACTIVE'
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

    // 이용권 상태 변경
    public boolean updateStatus(int memberId, String status) throws SQLException {
        String sql = """
                UPDATE MEMBER_TICKET
                SET status = ?
                WHERE member_id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, memberId);

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

            pstmt.setInt(1, memberTicketId);
            pstmt.setTimestamp(2, Timestamp.valueOf(startedAt));

            int rows = pstmt.executeUpdate();

            return rows > 0;
        }
    }

}
