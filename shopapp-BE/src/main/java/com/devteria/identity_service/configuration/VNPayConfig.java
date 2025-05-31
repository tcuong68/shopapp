package com.devteria.identity_service.configuration;

import com.devteria.identity_service.util.VNPayUtil;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.vnpay")
public class VNPayConfig {
    @Getter
    private String vnp_Payurl="https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private  String vnp_ReturnUrl="http://localhost:8080/shopapp/api/v1/payments/vnpay/callback";
    private  String vnp_TmnCode="H3LYQC3T";
    @Getter
    public  String secretKey="BULVNN90S2H6V5M31Y3OOBRPC4URYGBC";
    public  String vnp_Version="2.1.0";
    public  String  vnp_Command="pay";
    public  String orderType="other";

    public Map<String, String> getVNPayConfig() {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", vnp_Version);
        vnpParamsMap.put("vnp_Command", vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");
        vnpParamsMap.put("vnp_TxnRef", VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toan don hang:" +  VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderType", orderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", vnp_ReturnUrl);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_CreateDate", vnpCreateDate);
        calendar.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calendar.getTime());
        vnpParamsMap.put("vnp_ExpireDate", vnp_ExpireDate);
        return vnpParamsMap;
    }
    private String generateTransactionRef() {
        return "TXN" + System.currentTimeMillis();
    }


} 