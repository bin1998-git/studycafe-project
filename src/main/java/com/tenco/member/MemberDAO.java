package com.tenco.member;

import com.tenco.util.DBConnectionManager;

import java.lang.reflect.Member;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {


    // 회원 등록
    public boolean insert(MemberDTO member) throws SQLException {

        String sql = """
                    INSERT INTO MEMBER(name,phone,email,password) VALUES
                    (?, ?, ?, ?);
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, member.getName());
            pstm.setString(2, member.getPhone());
            pstm.setString(3, member.getEmail());
            pstm.setString(4, member.getPassword());


            int rows =pstm.executeUpdate();

            if (rows > 0) {
                return true;
            } else {
                return false;
            }

        }

    }

    //  회원 전체 조회
    public List<MemberDTO> getAllMember() throws SQLException {
        List<MemberDTO> memberDTOList = new ArrayList<>();

        String allSql = """
                SELECT * FROM MEMBER ORDER BY member_id
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(allSql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                memberDTOList.add(mapToMember(rs));
            }
        }

        return memberDTOList;
    }


    // 회원 아이디로 단건 조회
    public MemberDTO findById(int memberId) throws SQLException {

        String searchSql = """
                SELECT * FROM MEMBER WHERE member_id = ?
                """;


        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(searchSql)) {
            pstm.setInt(1, memberId);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return mapToMember(rs);
                }
            }
        }
        return null;
    }

    // 이름으로 검색
    public List<MemberDTO> findByName(String name) throws SQLException {

        List<MemberDTO> memberDTOList = new ArrayList<>();
        String nameSql = """
               SELECT * FROM MEMBER WHERE name = ? ORDER BY name
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(nameSql)) {
            pstm.setString(1, name);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    memberDTOList.add(mapToMember(rs));
                }
                return memberDTOList;
            }
        }

    }

    // 전화번호 중복 체크
    public boolean existsByPhone(String phone) throws SQLException {
        String sql = """
                    SELECT COUNT(*) FROM MEMBER WHERE phone = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, phone);

            try (ResultSet rs = pstm.executeQuery()) {
               if (rs.next()) {
                   return rs.getInt(1) > 0;
               }
            }
        }
        return false;
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = """
                    SELECT COUNT(*) FROM MEMBER WHERE email = ?
                """;
        try (Connection conn = DBConnectionManager.getConnection();
            PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, email);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }




    // 회원 정보 수정
    public boolean update(MemberDTO member) throws SQLException {
        String updateSql = """
                    UPDATE MEMBER
                    SET name = ?, phone = ?, email = ?, password = ?
                    WHERE member_id = ?
                
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstm = conn.prepareStatement(updateSql)) {
            pstm.setString(1,member.getName());
            pstm.setString(2, member.getPhone());
            pstm.setString(3, member.getEmail());
            pstm.setString(4, member.getPassword());
            pstm.setInt(5, member.getMemberId());

            int rows =pstm.executeUpdate();

            if (rows > 0) {
                return true;
            } else {
                return false;
            }
        }

    }

    // 회원정보 삭제
        public boolean delete(int id) throws SQLException {

            String deleteSql = """
                    DELETE FROM MEMBER WHERE member_id =?
                    """;

            try (Connection conn = DBConnectionManager.getConnection();
                PreparedStatement pstm = conn.prepareStatement(deleteSql)) {
                pstm.setInt(1, id);
                int rows = pstm.executeUpdate();
                return rows > 0;
            }
        }




    public MemberDTO mapToMember(ResultSet rs) throws SQLException {
        return MemberDTO.builder()
                .memberId(rs.getInt("member_id"))
                .name(rs.getString("name"))
                .phone(rs.getString("phone"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();

    }


}
