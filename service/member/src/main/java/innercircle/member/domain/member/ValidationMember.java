package innercircle.member.domain.member;

import java.time.LocalDate;


public class ValidationMember {


    protected static void validationNameCheck(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수로 입력해야합니다.");
        }

        if (name.length() > 10) {
            throw new IllegalArgumentException("이름은 10글자를 넘을 수 없습니다. name : " + name);
        }
    }

    protected static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수값인가요. password : " + password);
        }
    }

    protected static void validBirthDate(String birthDate) {
        try {
            LocalDate.parse(birthDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. birth_date : " + birthDate);
        }
    }

    protected static void validGender(String gender) {
        try {
            Gender.valueOf(gender);
        } catch (Exception e) {
            throw new IllegalArgumentException("성별 형식 올바르지 않습니다. gender : " + gender);
        }
    }

}
