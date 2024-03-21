package com.Backend.HarvestMaster.Order.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PendingDeliveryResponse {
    /*@JsonProperty("order_Status")
    private String orderStatus;

    @JsonProperty("payment_status")
    private String paymentStatus;*/

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("order_date")
    private String orderDate;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @JsonProperty("pickup_address")
    private String pickupAddress;

    @JsonProperty("delivery_date")
    private LocalDateTime deliveryDate;

    @JsonProperty("delivery_id")
    private Long deliveryId;

}