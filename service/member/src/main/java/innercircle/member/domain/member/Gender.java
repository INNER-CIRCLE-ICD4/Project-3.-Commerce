package innercircle.member.domain.member;

import lombok.Getter;

@Getter
public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private String koreaName;

    Gender(String koreaName) {
        this.koreaName = koreaName;
    }
}
