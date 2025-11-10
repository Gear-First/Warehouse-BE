package com.gearfirst.warehouse.common.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserContext {
    private final String userId;
    private final String username;
    private final String rank;
    private final String region;
    private final String workType;
}
