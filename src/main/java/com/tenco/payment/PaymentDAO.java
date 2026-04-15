package com.tenco.payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tenco.util.DBConnectionManager;

public class PaymentDAO {

  public boolean insert(PaymentDTO paymentDTO) {
    // 결제등록 (트랜잭션 사용)
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = DBConnectionManager.getConnection();
      // 트랜잭션 시작
      conn.setAutoCommit(false);

      String sql = "INSERT INTO PAYMENT (member_id, ticket_id, amount, method, status, paid_at) "
          + "VALUES (?, ?, ?, ?, ?, NOW())";

      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, paymentDTO.getMember_id());
      pstmt.setInt(2, paymentDTO.getTicket_id());
      pstmt.setInt(3, paymentDTO.getAmount());
      pstmt.setString(4, paymentDTO.getMethod());
      pstmt.setString(5, paymentDTO.getStatus());
      int result = pstmt.executeUpdate();
      // 커밋
      conn.commit();

      return result > 0;

    } catch (SQLException e) {
      // 롤백
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException rollbackEx) {
          rollbackEx.printStackTrace();
        }
      }
      e.printStackTrace();
      return false;

    } finally {
      // 리소스 정리
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  // 전체 결제 내역 조회
  public List<PaymentDTO> findAll() throws SQLException {
    List<PaymentDTO> paymentDTOList = new ArrayList<>();
    String findSql = """
        SELECT * FROM PAYMENT ORDER BY PAID_AT
        """;
    try (Connection conn = DBConnectionManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(findSql);
         ResultSet rs = pstmt.executeQuery()) {
      while (rs.next()) {
        paymentDTOList.add(mapToPaymentDTO(rs));
      }
    }
    return paymentDTOList;
  }

  // 회원별 결제 내역 조회
  public List<PaymentDTO> findByMemberid(int member_id) throws SQLException {
    List<PaymentDTO> paymentDTOList = new ArrayList<>();
    String findByMemberidSql = """
        SELECT * FROM PAYMENT WHERE MEMBER_ID = ?
        """;
    try (Connection conn = DBConnectionManager.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(findByMemberidSql)) {
      pstmt.setInt(1, member_id);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          paymentDTOList.add(mapToPaymentDTO(rs));
        }
      }
    }
    return paymentDTOList;
  }


  // 결제 상태 변경 (환불 등)
  public boolean updateStatus(int payment_id, String status) {
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = DBConnectionManager.getConnection();
      // 트랜잭션 시작
      conn.setAutoCommit(false);

      String sql = "UPDATE PAYMENT SET status = ? WHERE payment_id = ?";

      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, status);
      pstmt.setInt(2, payment_id);
      int result = pstmt.executeUpdate();

      // 커밋
      conn.commit();

      return result > 0;

    } catch (SQLException e) {
      // 롤백
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException rollbackEx) {
          rollbackEx.printStackTrace();
        }
      }
      e.printStackTrace();
      return false;

    } finally {
      // 리소스 정리
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private PaymentDTO mapToPaymentDTO(ResultSet rs) throws SQLException {

    return PaymentDTO.builder()
        .payment_id(rs.getInt("payment_id"))
        .member_id(rs.getInt("member_id"))
        .ticket_id(rs.getInt("ticket_id"))
        .amount(rs.getInt("amount"))
        .method(rs.getString("method"))
        .status(rs.getString("status"))
        .paidAt(rs.getTimestamp("paid_at").toLocalDateTime())
        .build();

  }
}
