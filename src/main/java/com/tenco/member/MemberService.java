package com.tenco.member;

import java.sql.SQLException;
import java.util.List;

public class MemberService {
    private final MemberDAO memberDAO = new MemberDAO();

    // 유효성 검사 후 회원 등록 (이름. 전화번호. 이메일. 비밀번호)
    public boolean registerMember(MemberDTO member) throws SQLException {
        if (member.getName() == null || member.getName().isBlank()) {
            throw new SQLException("이름입력은 필수 입니다");
        }

        if (member.getPassword() == null || member.getPassword().isBlank()) {
            throw new RuntimeException("비밀번호 입력은 필수 입니다");

        }

        // 2. 중복 체크
        if (memberDAO.existsByPhone(member.getPhone())) {
            System.out.println("이미 사용 중인 전화번호입니다.");
            return false;
        }
        if (memberDAO.existsByEmail(member.getEmail())) {
            System.out.println("이미 사용 중인 이메일입니다.");
            return false;
        }


        return memberDAO.insert(member);
    }


    // 전체 회원목록 반환
    public List<MemberDTO> getMemberList() throws SQLException {
        return memberDAO.getAllMember();

    }

    // ID로 회원 조회, 없으면 예외
    public MemberDTO getMemberById(int id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("회원 정보가 없습니다");
        }
        return memberDAO.findById(id);

    }

    // 이름 검색
    public List<MemberDTO> searchByName(String name) throws SQLException {
        if (name == null || name.isBlank()) {
             throw new SQLException("이름을 입력해주세요");
        }
        return memberDAO.findByName(name);
    }

    // 회원 정보 수정
    public boolean modifyMember(MemberDTO member) throws SQLException {
        return memberDAO.update(member);

    }

    // 회원 정보 삭제
    public boolean removeMember(int id) throws SQLException {
        return memberDAO.delete(id);
    }


}
