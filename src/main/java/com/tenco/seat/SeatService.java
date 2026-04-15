package com.tenco.seat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeatService {

    private final SeatDAO seatDAO = new SeatDAO();

    // 좌석 등록
    public boolean addSeat(SeatDTO seatDTO) throws SQLException {
        if (seatDTO.getSeatNumber() == null) {
            throw new SQLException("좌석 번호는 필수 사항 입니다!");
        }
        if (seatDTO.getSeatType() == null) {
            throw new SQLException("좌석의 종류(시간권 / 기간권)는 필수 사항 입니다!");
        }

        seatDAO.insert(seatDTO);
        return true;
    }

    // 전체 좌석 리스트 조회
    public List<SeatDTO> getSeatList() throws SQLException {
        return seatDAO.findAll();
    }

    // 사용 가능 자리 리스트 조회
    public List<SeatDTO> getAvailableSeats() throws SQLException {
        return seatDAO.findAvailable();
    }

    // 좌석 상태 변경
    public boolean modifySeatStatus(int seatId, Status status) throws SQLException {
        if (status == null) {
            throw new SQLException("좌석 상태는 필수 사항 입니다.");
        }
        if (seatId <= 0 ) {
            throw new SQLException("seatId는 0이거나 음수일 수 없습니다.");
        }

        return seatDAO.updateStatus(seatId, status);
    }

    // 좌석 제거
    public boolean removeSeat(int seatId) throws SQLException {
        if (seatId <= 0) {
            throw new SQLException("seatId 가 틀렸습니다.");
        }

        return seatDAO.delete(seatId);
    }

    // usageDTO, usageDAO 설계 후 구현
    public boolean checkIn(int memberId, int seatId) {
        return false;
    }

    // usageDTO, usageDAO 설계 후 구현
    public boolean checkOut(int usageId) {
        return false;
    }


}
