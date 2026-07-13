package com.teamcity.api.models.dto_models.auth;

import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountResponse extends BaseModel {
    private Long id;
    private String accountNumber;
    private Double balance;
    private String currency;
    private String status;
    private Long customerId;
}