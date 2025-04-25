package com.devteria.identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor //tao constructor k co tham so
@AllArgsConstructor //tao constructor co tham so day du
@Builder //khong can day du cac truong van tao duoc Object
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSearchRequest {
    String username;
    String status;
    String phoneNumber;
    String address;
    Long minTotalPrice;
    Long maxTotalPrice;
}
