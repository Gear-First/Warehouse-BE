package com.gearfirst.warehouse.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDto {
    private Long id;
    private String eventId;
    private String type;
    private String message;
    private String receiver;
    private boolean read = false;
}
