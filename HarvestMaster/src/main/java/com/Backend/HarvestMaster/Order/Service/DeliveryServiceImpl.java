package com.Backend.HarvestMaster.Order.Service;

import com.Backend.HarvestMaster.Cart.Model.CartItem;
import com.Backend.HarvestMaster.Cart.Repository.CartRepository;
import com.Backend.HarvestMaster.Inventory.Repository.InventoryRepository;
import com.Backend.HarvestMaster.Order.Model.*;
import com.Backend.HarvestMaster.Order.Repository.DeliveryLogActivityRepository;
import com.Backend.HarvestMaster.Order.Repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final CartRepository cartRepository;
    private final DeliveryLogActivityRepository logActivityRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public CommonResponse updateDeliverySchedule(DeliveryRequest delivery) {
        Delivery deliveryData = deliveryRepository.findDeliveryByDeliveryId(delivery.getDeliveryId());

        if (deliveryData != null) {
            deliveryData.setDeliveryAddress(delivery.getDeliveryAddress());
            deliveryData.setPickupAddress(delivery.getPickupAddress());
            deliveryRepository.save(deliveryData);

            LogActivity logActivity = LogActivity.builder()
                    .deliveryId(deliveryData)
                    .details("Order Updated")
                    .cartId(deliveryData.getCartId())
                    .build();
            logActivityRepository.save(logActivity);

            return CommonResponse.builder()
                    .message("Updated")
                    .status(true)
                    .build();
        } else {
            return CommonResponse.builder()
                    .message("User not found!")
                    .status(false)
                    .build();
        }
    }


    @Override
    public CommonResponse markAsDelivered(DeliveryConfirmRequest delivery) {
        Delivery deliveryData = deliveryRepository.findDeliveryByDeliveryId(delivery.getDeliveryId());

        if (deliveryData != null) {
            deliveryData.setDeliveryStatus("DELIVERED");
            deliveryRepository.save(deliveryData);

            LogActivity logActivity = LogActivity.builder()
                    .deliveryId(deliveryData)
                    .details("Order Delivered")
                    .cartId(deliveryData.getCartId())
                    .build();
            logActivityRepository.save(logActivity);

            return CommonResponse.builder()
                    .message("Delivered")
                    .status(true)
                    .build();
        } else {
            return CommonResponse.builder()
                    .message("Delivery not found!")
                    .status(false)
                    .build();
        }
    }

    @Override
    public CommonResponse createNewDelivery(DeliveryCreateRequest request) {

        CartItem cartDetails = cartRepository.findById(request.getOrderId()).get();

        Delivery deliveryData = Delivery.builder()
                .customerName(request.getCustomerName())
                .deliveryAddress(request.getDeliveryAddress())
                .pickupAddress(request.getPickupAddress())
//                 .deliveryDate(request.getDeliveryDate())
                .driverId(request.getDriverId())
                .driverName(request.getDriverName())
                .vehicleNumber(request.getVehicleNumber())
                .orderStatus("PENDING")
                .paymentStatus("PENDING")
                .buyer(cartDetails.getBuyer())
                .cartId(request.getOrderId())
                .build();


        deliveryData = deliveryRepository.save(deliveryData);

        return CommonResponse.builder()
                .status(true)
                .message("Data Saved")
                .data(deliveryData)
                .build();
    }

    @Override
    public CommonResponse getPendingDelivery(PendingDeliveryRequest request) {
        List<Delivery> deliveries = deliveryRepository.findDeliverysByOrderStatusAndPaymentStatusAndDeliveryStatus(request.getOrderStatus(), request.getPaymentStatus(), "PENDING");
        List<PendingDeliveryResponse> pendingDeliveries = new ArrayList<>();

        for (Delivery item : deliveries) {
            pendingDeliveries.add(
                    PendingDeliveryResponse.builder()
                            .customerName(item.getBuyer().getCusName())
                            .orderId(String.valueOf(item.getCartId()))
                            .orderDate(item.getOrderDate().toString())
                            .deliveryAddress(item.getDeliveryAddress())
                            .pickupAddress(item.getPickupAddress())
                            .deliveryDate(item.getDeliveryDate())
                            .deliveryId(item.getDeliveryId())
                            .build()
            );
        }

        return CommonResponse.builder()
                .status(true)
                .message("Pending Deliveries")
                .data(pendingDeliveries)
                .build();

    }

    @Override
    public CommonResponse manageDeliveries(ManageDeliveryRequest request) {
        Optional<Delivery> delivery = deliveryRepository.findById(request.getDeliveryId());
        if (delivery.isEmpty()) {
            return CommonResponse.builder()
                    .status(false)
                    .message("Delivery not found")
                    .data(null)
                    .build();
        }
        Delivery deliveryDetails = delivery.get();
        deliveryDetails.setOrderStatus(request.isOrderStatus() ? "APPROVED" : "REJECTED");
        deliveryDetails.setReason(request.getReason());
//        deliveryDetails.setReason(StringUtils.hasLength(request.getReason())?request.getReason():null);
        deliveryRepository.save(deliveryDetails);
        return CommonResponse.builder()
                .status(true)
                .message("Delivery Managed")
                .data(null)
                .build();
    }

    @Override
    public CommonResponse deliveryLogActivity() {
        ArrayList<LogActivityResponse> list = new ArrayList<>();

        List<LogActivity> activities = logActivityRepository.findAll();

        for (LogActivity activity : activities) {
            list.add(
                    LogActivityResponse.builder()
                            .date(activity.getDate().toLocalDate().toString())
                            .time(activity.getDate().toLocalTime().toString())
                            .detail(activity.getDetails())
                            .cartId(activity.getCartId())
                            .build()
            );
        }


        return CommonResponse.builder()
                .status(true)
                .message("Delivery Log Activities")
                .data(list)
                .build();
    }

    @Override
    public CommonResponse orderTotal() {
        long pendingCount = deliveryRepository.countByDeliveryStatus("PENDING");
        long deliveredCount = deliveryRepository.countByDeliveryStatus("DELIVERED");
        long inventoryCount = inventoryRepository.count();
        System.out.println("pendingCount : " + pendingCount);
        System.out.println("deliveredCount : " + deliveredCount);
        System.out.println("inventory_count : " + inventoryCount);

        HashMap<String, Object> map = new HashMap<>();
        map.put("pending_count", pendingCount);
        map.put("delivered_count", deliveredCount);
        map.put("inventory_count", inventoryCount);

        return CommonResponse.builder()
                .status(true)
                .message("Total")
                .data(map)
                .build();
    }
}
