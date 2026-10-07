package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Format response VNPAY yeu cau tai endpoint IPN */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VnPayIpnResponse {

    private String RspCode;
    private String Message;

    public static VnPayIpnResponse success() {
        return new VnPayIpnResponse("00", "Confirm Success");
    }

    public static VnPayIpnResponse orderNotFound() {
        return new VnPayIpnResponse("01", "Order not found");
    }

    public static VnPayIpnResponse amountInvalid() {
        return new VnPayIpnResponse("04", "Amount invalid");
    }

    public static VnPayIpnResponse orderAlreadyConfirmed() {
        return new VnPayIpnResponse("02", "Order already confirmed");
    }

    public static VnPayIpnResponse checksumFail() {
        return new VnPayIpnResponse("97", "Invalid Checksum");
    }
}
