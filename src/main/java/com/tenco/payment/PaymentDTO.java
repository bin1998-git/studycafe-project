package com.tenco.payment;

import lombok.*;

import java.time.LocalDateTime;
@Builder
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

  private int payment_id; // 결제ID (PK)
  private int member_id; // 회원ID (FK)
  private int ticket_id; // 이용권 상품 ID (FK)
  private int amount; // 결제금액
  private String method; // card, cash, tansfer
  private String status; // succeess, fail, refund
  private LocalDateTime paidAt; // 결제 시각

}