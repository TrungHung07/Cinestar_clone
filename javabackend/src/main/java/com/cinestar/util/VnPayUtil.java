package com.cinestar.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Tien ich VNPAY: ky HMAC-SHA512 + build URL thanh toan + kiem tra checksum.
 * Theo chuan cua VNPAY:
 * - Khi TAO URL: hash data gom cac tham so da URL-encode (US-ASCII).
 * - Khi XAC MINH IPN/Return: hash data gom gia tri GOC (chua decode/encode).
 */
public class VnPayUtil {

    private VnPayUtil() {
    }

    public static String hmacSHA512(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Khong the ky HMAC-SHA512", ex);
        }
    }

    /** Build query string da sort theo key + URL-encode value (dung khi tao URL thanh toan) */
    public static String buildHashData(Map<String, String> params, boolean urlEncode) {
        SortedMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append('&');
            }
            first = false;
            sb.append(entry.getKey()).append('=');
            sb.append(urlEncode
                    ? URLEncoder.encode(value, StandardCharsets.US_ASCII)
                    : value);
        }
        return sb.toString();
    }

    /** Ky checksum tren gia tri GOC (decoded) - cach VNPAY sample code lam voi IPN */
    public static String computeChecksum(Map<String, String> params, String secretKey) {
        Map<String, String> data = new java.util.HashMap<>(params);
        data.remove("vnp_SecureHash");
        data.remove("vnp_SecureHashType");
        return hmacSHA512(secretKey, buildHashData(data, false));
    }

    /**
     * Ky checksum tren gia tri DA URL-ENCODE - mot so phien ban VNPAY (Return)
     * ky theo kieu nay. Verify se thu ca 2 cach de tranh lech chuan.
     */
    public static String computeChecksumEncoded(Map<String, String> params, String secretKey) {
        Map<String, String> data = new java.util.HashMap<>(params);
        data.remove("vnp_SecureHash");
        data.remove("vnp_SecureHashType");
        return hmacSHA512(secretKey, buildHashData(data, true));
    }

    /** Du lieu vi bao mat: tao TxnRef ngau nhien khoi doan */
    public static String randomRef(String prefix) {
        return prefix + System.currentTimeMillis();
    }
}
