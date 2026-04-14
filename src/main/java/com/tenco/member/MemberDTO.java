package com.tenco.member;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MemberDTO {
    private int memberId;
    private String name;
    private String phone;
    private String email;
    private String password;
    private LocalDateTime createdAt;

}
