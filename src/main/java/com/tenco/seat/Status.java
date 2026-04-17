package com.tenco.seat;

// DB 의 SEAT.status ENUM('AVAILABLE','IN_USE','DISABLED') 와 1:1 매핑
public enum Status {
    AVAILABLE, // 사용 가능
    IN_USE,    // 사용 중
    DISABLED   // 사용 불가(점검/고장 등)
}
