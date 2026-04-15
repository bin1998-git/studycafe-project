package com.tenco.seat_usage;

import com.tenco.payment.PaymentDTO;
import com.tenco.util.DBConnectionManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor

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

    // 퇴실 처리 ended_at 기록
    public boolean updateEndedAt(int memberId)  throws SQLException {
        return false;
    }

    // 전체 이용 내역 조회
    public List<SeatUsageDTO> findAll() throws SQLException {
        return null;
    }

    // 회원별 이용 내역 조회
    public List<SeatUsageDTO> findByMemberId(int memberId) throws SQLException {
        List<SeatUsageDTO> seatUsageDTOList = new ArrayList<>();
        String findByMemberidSql = """
        SELECT * FROM SEATUSAGE WHERE member_id = ?
        """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(findByMemberidSql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SeatUsageDTO dto = SeatUsageDTO.builder()
                        .usageId(rs.getInt("usage_id"))
                        .memberId(rs.getInt("member_id"))
                        .seatId(rs.getInt("seat_id"))
                        .memberTicketId(rs.getInt("member_ticket_id"))
                        .startTime(rs.getTimestamp("started_at").toLocalDateTime())
                        // ended_at은 null일 수 있으므로 체크 필요
                        .endTime(rs.getTimestamp("ended_at") != null ?
                            rs.getTimestamp("ended_at").toLocalDateTime() : null)
                        .build();

                    seatUsageDTOList.add(dto);
                }
            }
        }
        return seatUsageDTOList;
    }


}
