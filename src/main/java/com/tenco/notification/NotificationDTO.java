package com.tenco.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class NotificationDTO {
    private int notificationId;      // 알림 ID (PK)
    private int memberId;             // 수신 회원 ID (FK)
    private String type;              // SEAT_START / SEAT_END / PAYMENT_DONE
    private String message;           // 알림 내용
    private int relatedId;            // 관련 ID (usage_id 또는 payment_id)
    private boolean isRead;           // 읽음 여부 (false: 미읽음)
    private LocalDateTime createdAt;  // 생성 시각
}
