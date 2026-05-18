package com.study.neighbortrade.support;

/**
 * 관리자 화면 등에서 이메일 노출 시 최소 정보만 보이도록 마스킹한다.
 * 원문 이메일은 로그에 남기지 않는 것을 권장한다({@code EmailMasking} 적용 후 출력).
 */
public final class EmailMasking {

    private EmailMasking() {}

    /**
     * 로컬파트 첫 글자 + *** + @ + 도메인 전체.
     * 예: {@code seller@neighbortrade.local} → {@code s***@neighbortrade.local}
     */
    public static String mask(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 0 || at == trimmed.length() - 1) {
            return "***";
        }
        String local = trimmed.substring(0, at);
        String domain = trimmed.substring(at + 1);
        if (local.isEmpty()) {
            return "***@" + domain;
        }
        String maskedLocal = local.length() == 1 ? "*" : local.charAt(0) + "***";
        return maskedLocal + "@" + domain;
    }
}
