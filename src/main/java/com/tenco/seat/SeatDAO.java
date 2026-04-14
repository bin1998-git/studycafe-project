package com.tenco.seat;



import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {

    // 사용중인 자리와 사용중이지 않은 자리 전체를 조회 해서 보여줌
    public List<SeatDTO> findAll() throws SQLException {
        List<SeatDTO> seatList = new ArrayList<>();
        String sql = """
        SELECT * FROM seat;
        """;
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                seatList.add(mapToSeat(rs));
            }
        }
        return seatList;
    }

    // seatId 로 좌석 한 건 조회
    public SeatDTO findSeatById(int seatId) throws SQLException {
        SeatDTO seatDTO  = new SeatDTO();
        String sql = """
                SELECT * FROM seat WHERE seat_id = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, seatId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                seatDTO = mapToSeat(rs);
            }
        }
        return seatDTO;
    }

    // 사용 가능한 자리 모두 조회
    public List<SeatDTO> findAvailable() {
        return null;
    }

    // 좌석 추가
    public boolean insert(SeatDTO seatDTO) {
        return false;
    }

    // 좌석 상태 변경 (빈자리, 사용중인 자리등등)
    public boolean updateStatus(int seatId, Status status) {
        return false;
    }

    // 좌석 삭제
    public boolean delete(int seatId) {
        return false;
    }


    // 좌석 빌더
    private SeatDTO mapToSeat(ResultSet rs) throws SQLException {
        return SeatDTO.builder()
                .seatId(rs.getInt("seat_id"))
                .seatNumber(rs.getString("seat_number"))
                .seatType(rs.getObject("seat_type", SeatType.class))
                .status(rs.getObject("status", Status.class))
                .zone(rs.getString("zone"))
                .build();
    }
}
