package com.lambda.stocksubscription.user;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserDTO {
    private Long id;
    private String email;
    private boolean subscribed;
    private List<String> interestedStocks;
}
