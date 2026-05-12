package com.study.neighbortrade.domain.product;

public enum ProductCategory {
    DIGITAL("디지털기기"), APPLIANCE("생활가전"), FURNITURE("가구/인테리어"), KITCHEN("생활/주방"), KIDS("유아동"), FASHION("패션/잡화"), BEAUTY("뷰티/미용"), SPORTS("스포츠/레저"), HOBBY("취미/게임/음반"), BOOK("도서"), TICKET("티켓/교환권"), PET("반려동물용품"), PLANT("식물"), ETC("기타 중고물품"), WANTED("삽니다");
    private final String label;
    ProductCategory(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}
