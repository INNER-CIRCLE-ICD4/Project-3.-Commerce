package innercircle.member.domain.member;

import lombok.Getter;

@Getter
public enum Gender {
    MAIL("남성"),
    FEMAIL("여성");

    private String koreaName;

    Gender(String koreaName) {
        this.koreaName = koreaName;
    }
}
