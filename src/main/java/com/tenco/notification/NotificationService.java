package com.tenco.notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private NotificationDAO notificationDAO = new NotificationDAO();

    // 알림 전송 (생성)
    public boolean sendNotification(int memberId, String type, int relatedId) {
        try {
            String message = generateMessage(type);

            NotificationDTO notification = NotificationDTO.builder()
                    .memberId(memberId)
                    .type(type)
                    .message(message)
                    .relatedId(relatedId)
                    .isRead(false)
                    .build();

            return notificationDAO.insert(notification);
        } catch (SQLException e) {
            System.err.println("알림 생성 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    // 회원별 모든 알림 조회
    public List<NotificationDTO> getNotifications(int memberId) {
        try {
            return notificationDAO.findByMemberId(memberId);
        } catch (SQLException e) {
            System.err.println("알림 조회 중 오류 발생: " + e.getMessage());
            return List.of();
        }
    }

    // 회원별 미읽은 알림 조회
    public List<NotificationDTO> getUnreadNotifications(int memberId) {
        try {
            return notificationDAO.findUnreadByMemberId(memberId);
        } catch (SQLException e) {
            System.err.println("미읽은 알림 조회 중 오류 발생: " + e.getMessage());
            return List.of();
        }
    }

    // 알림 읽음 처리
    public boolean readNotification(int notificationId) {
        try {
            return notificationDAO.markAsRead(notificationId);
        } catch (SQLException e) {
            System.err.println("알림 읽음 처리 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    // 타입에 따른 메시지 생성
    private String generateMessage(String type) {
        return switch (type) {
            case "SEAT_START" -> "좌석 사용이 시작되었습니다.";
            case "SEAT_END" -> "좌석 사용이 종료되었습니다.";
            case "PAYMENT_DONE" -> "결제가 완료되었습니다.";
            default -> "알림입니다.";
        };
    }
}


