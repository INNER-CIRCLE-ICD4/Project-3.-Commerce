package innercircle.commerce.order.domain.model.vo;

import innercircle.commerce.order.application.port.in.command.PlaceOrderCommand;

/**
 * ShippingAddress Value Object
 * 배송 주소 정보
 */
public record ShippingAddress(
        String recipientName,
        String phoneNumber,
        String addressCode,
        String address,
        String addressDetail,
        String deliveryRequest
) {

    public static ShippingAddress create(PlaceOrderCommand.ShippingInfo s) {
        return new ShippingAddress(
                s.recipientName(),
                s.phoneNumber(),
                s.addressCode(),
                s.address(),
                s.addressDetail(),
                s.deliveryRequest()
        );
    }

    public ShippingAddress {
        validateRequiredFields(recipientName, phoneNumber, addressCode, address);
    }

    private static void validateRequiredFields(String recipientName, String phoneNumber, String zipCode, String address) {
        if (isNullOrEmpty(recipientName)) throw new IllegalArgumentException("Recipient name is required");
        if (isNullOrEmpty(phoneNumber)) throw new IllegalArgumentException("Phone number is required");
        if (isNullOrEmpty(zipCode)) throw new IllegalArgumentException("Zip code is required");
        if (isNullOrEmpty(address)) throw new IllegalArgumentException("Address is required");

        validatePhoneNumber(phoneNumber);
        validateZipCode(zipCode);
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void validatePhoneNumber(String phoneNumber) {
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (!cleanedNumber.matches("^01[0-9]{8,9}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    private static void validateZipCode(String zipCode) {
        if (!zipCode.matches("^[0-9]{5}$")) {
            throw new IllegalArgumentException("Invalid zip code format");
        }
    }

    public String fullAddress() {
        return "[" + addressCode + "] " + address + (addressDetail != null && !addressDetail.isEmpty() ? " " + addressDetail : "");
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", recipientName, phoneNumber, fullAddress());
    }
}
