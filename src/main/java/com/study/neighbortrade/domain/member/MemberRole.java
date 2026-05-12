package com.study.neighbortrade.domain.member;

public enum MemberRole {
    ROLE_USER("ROLE_USER"), ROLE_LOCAL_VERIFIED("ROLE_LOCAL_VERIFIED"), ROLE_ADMIN("ROLE_ADMIN");
    private final String value;
    MemberRole(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
