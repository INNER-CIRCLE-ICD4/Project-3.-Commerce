package innercircle.member.application.port.out;


import java.util.List;

public interface TokenPort {

    /**
     * Access Token 생성
    */
    String generateAccessToken(Long userId, String email, List<String> roles);


    /**
     * Refresh Token 생성
     */
    String generateRefreshToken(Long userId, String email, List<String> roles);

    /**
     *  토큰 검증
     */
    boolean validateToken(String token);

    /**
     * 토큰에서 사용자 정보 추출 - userId
     */
    Long getUserIdFromToken(String token);

    /**
     * 토큰에서 사용자 정보 추출 - email
     */
    String getEmailFromToken(String token);


    /**
     * 토큰에서 사용자 정보 추출 - roles
     */
    List<String> getRolesFromToken(String token);
}
