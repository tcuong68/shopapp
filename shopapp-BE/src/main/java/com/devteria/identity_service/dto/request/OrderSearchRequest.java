package com.devteria.identity_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor //tao constructor k co tham so
@AllArgsConstructor //tao constructor co tham so day du
@Builder //khong can day du cac truong van tao duoc Object
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderSearchRequest {
    Long id;
    String status;
    Date created_at;
    Long minTotalPrice;
    Long maxTotalPrice;
}
