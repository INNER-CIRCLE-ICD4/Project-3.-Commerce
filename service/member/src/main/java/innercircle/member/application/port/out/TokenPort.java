package innercircle.member.application.port.out;


import innercircle.member.domain.auth.AuthToken;

import java.util.List;

public interface TokenPort {

    /**
     * JWT 토큰 생성
     */
    AuthToken generateTokenPair(Long userId, String email, List<String> roles);

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

    /**
     * Refresh Token 검증
     */
    boolean isRefreshToken(String refreshToken);


}
