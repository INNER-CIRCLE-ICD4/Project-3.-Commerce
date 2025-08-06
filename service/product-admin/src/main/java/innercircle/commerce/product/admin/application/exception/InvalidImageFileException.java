package innercircle.commerce.product.admin.application.exception;

/**
 * 유효하지 않은 이미지 파일에 대한 예외
 */
public class InvalidImageFileException extends RuntimeException {
    
    private final String fileName;
    private final String reason;
    
    public InvalidImageFileException(String fileName, String reason) {
        this.fileName = fileName;
        this.reason = reason;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public String getReason() {
        return reason;
    }
}