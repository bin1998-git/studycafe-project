package com.tenco.seat;

// db 에서 만든 enum 타입의 컬럼을 받을 enum class 설계
public enum Status {
    AVAILABLE, // 사용 가능 자리 판단
    IN_USE, // 사용 중인 자리 판단
    DISABLE // 사용 불가능 자리 판단
}
