package com.tenco.ticket;

import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    private TicketDTO mapToTicket(ResultSet rs) throws SQLException {
        return TicketDTO.builder()
                .ticketId(rs.getInt("ticket_id"))
                .name(rs.getString("name"))
                .type(TicketType.valueOf(rs.getString("type")))
                .durationValue(rs.getInt("duration_value"))
                .price(rs.getInt("price"))
                .description(rs.getString("description"))
                .build();
    }

    // 전체 이용권 상품 조회
    public List<TicketDTO> findAll() throws SQLException {
        List<TicketDTO> ticketList = new ArrayList<>();
        String sql = """
                SELECT * FROM ticket ORDER BY ticket_id
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ticketList.add(mapToTicket(rs));
            }
            return ticketList;
        }
    }

    // ID로 1건 조회
    public TicketDTO findById(int ticketId) throws SQLException {
        String sql = """
                SELECT * FROM ticket WHERE ticket_id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToTicket(rs);
                }
            }
        }
        return null;
    }

    // 이용권 상품 등록
    public boolean insert(TicketDTO ticketDTO) throws SQLException {
        String sql = """
                INSERT INTO TICKET (name, type, duration_value, price, description)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ticketDTO.getName());
            pstmt.setString(2, ticketDTO.getType().name());
            pstmt.setInt(3, ticketDTO.getDurationValue());
            pstmt.setInt(4, ticketDTO.getPrice());
            pstmt.setString(5, ticketDTO.getDescription());

            int rows = pstmt.executeUpdate();

            return rows > 0;

        }
    }

    // 이용권 상품 수정
    public boolean update(TicketDTO ticketDTO) throws SQLException {
        String sql = """
                UPDATE ticket
                SET name = ?,
                	type = ?,
                    duration_value = ?,
                    price = ?,
                    description = ?
                WHERE ticket_id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ticketDTO.getName());
            pstmt.setString(2, ticketDTO.getType().name());
            pstmt.setInt(3, ticketDTO.getDurationValue());
            pstmt.setInt(4, ticketDTO.getPrice());
            pstmt.setString(5, ticketDTO.getDescription());

            int rows = pstmt.executeUpdate();

            return rows > 0;
        }
    }

    // 이용권 상품 삭제
    public boolean delete(int ticketId) throws SQLException {
        String sql = """
                DELETE FROM ticket WHERE ticekt_id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ticketId);

            int rows = pstmt.executeUpdate();

            return rows > 0;
        }
    }
}
