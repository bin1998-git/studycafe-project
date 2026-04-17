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
        if (member.getPhone() == null || member.getPhone().isBlank()) {
            throw new IllegalArgumentException("전화번호 입력은 필수입니다");
        }
        if (member.getEmail() == null || member.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일 입력은 필수입니다");
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
        MemberDTO member = memberDAO.findById(id);
        if (member == null) throw new SQLException("존재하지 않는 회원입니다");
        return member;


    }

    // 전화번호로 회원 조회 (없으면 null)
    public MemberDTO findByPhone(String phone) throws SQLException {
        if (phone == null || phone.isBlank()) return null;
        return memberDAO.findByPhone(phone);
    }

    /**
     * 숫자(회원 ID) 또는 전화번호(010-xxxx-xxxx, 01011112222 모두 허용)
     * 로 회원을 찾아 member_id 를 반환. 없으면 -1.
     */
    public int resolveMemberId(String input) throws SQLException {
        if (input == null) return -1;
        String v = input.trim();
        if (v.isEmpty()) return -1;

        // 전화번호로 판별: '-' 포함 또는 길이 8자리 이상의 숫자열
        String digits = v.replaceAll("[^0-9]", "");
        boolean looksLikePhone = v.contains("-") || digits.length() >= 9;

        if (!looksLikePhone) {
            try {
                int id = Integer.parseInt(v);
                MemberDTO m = memberDAO.findById(id);
                if (m != null) return m.getMemberId();
            } catch (NumberFormatException ignored) {}
        }
        // 전화번호 조회
        MemberDTO m = memberDAO.findByPhone(v);
        return (m == null) ? -1 : m.getMemberId();
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
        if (member == null) throw new IllegalArgumentException("회원 정보가 없습니다");
        return memberDAO.update(member);

    }

    // 회원 정보 삭제
    public boolean removeMember(int id) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("유효하지 않은 ID입니다");
        return memberDAO.delete(id);
    }


}
