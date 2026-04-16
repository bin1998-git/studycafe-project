package com.tenco.notification;

import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    // 알림 생성
    public boolean insert(NotificationDTO notification) throws SQLException {
        String sql = """
                INSERT INTO NOTIFICATION (member_id, type, message, related_id, is_read, created_at) 
                VALUES (?, ?, ?, ?, ?, NOW())
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, notification.getMemberId());
            pstm.setString(2, notification.getType());
            pstm.setString(3, notification.getMessage());
            pstm.setInt(4, notification.getRelatedId());
            pstm.setBoolean(5, notification.isRead());

            int rows = pstm.executeUpdate();
            return rows > 0;
        }
    }

    // 회원별 알림 전체 조회
    public List<NotificationDTO> findByMemberId(int memberId) throws SQLException {
        List<NotificationDTO> notificationList = new ArrayList<>();

        String sql = """
                SELECT * FROM NOTIFICATION 
                WHERE member_id = ? 
                ORDER BY created_at DESC
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, memberId);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    notificationList.add(mapToNotification(rs));
                }
            }
        }

        return notificationList;
    }

    // 회원별 미읽은 알림 조회
    public List<NotificationDTO> findUnreadByMemberId(int memberId) throws SQLException {
        List<NotificationDTO> notificationList = new ArrayList<>();

        String sql = """
                SELECT * FROM NOTIFICATION 
                WHERE member_id = ? AND is_read = false
                ORDER BY created_at DESC
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, memberId);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    notificationList.add(mapToNotification(rs));
                }
            }
        }

        return notificationList;
    }

    // 알림 읽음 처리
    public boolean markAsRead(int notificationId) throws SQLException {
        String sql = """
                UPDATE NOTIFICATION 
                SET is_read = true 
                WHERE notification_id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, notificationId);

            int rows = pstm.executeUpdate();
            return rows > 0;
        }
    }

    // ResultSet을 NotificationDTO로 변환
    private NotificationDTO mapToNotification(ResultSet rs) throws SQLException {
        return NotificationDTO.builder()
                .notificationId(rs.getInt("notification_id"))
                .memberId(rs.getInt("member_id"))
                .type(rs.getString("type"))
                .message(rs.getString("message"))
                .relatedId(rs.getInt("related_id"))
                .isRead(rs.getBoolean("is_read"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}

