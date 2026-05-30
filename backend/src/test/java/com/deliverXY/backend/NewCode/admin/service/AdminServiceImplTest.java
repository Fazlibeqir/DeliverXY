package com.deliverXY.backend.NewCode.admin.service;

import com.deliverXY.backend.NewCode.admin.service.impl.AdminServiceImpl;
import com.deliverXY.backend.NewCode.common.enums.DeliveryStatus;
import com.deliverXY.backend.NewCode.common.enums.PaymentStatus;
import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryRepository;
import com.deliverXY.backend.NewCode.deliveries.service.DeliveryService;
import com.deliverXY.backend.NewCode.drivers.repository.DriverLocationRepository;
import com.deliverXY.backend.NewCode.exceptions.BadRequestException;
import com.deliverXY.backend.NewCode.exceptions.NotFoundException;
import com.deliverXY.backend.NewCode.kyc.service.AppUserKYCService;
import com.deliverXY.backend.NewCode.payments.domain.Payment;
import com.deliverXY.backend.NewCode.payments.repository.PaymentRepository;
import com.deliverXY.backend.NewCode.payments.service.PaymentService;
import com.deliverXY.backend.NewCode.user.service.AppUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AppUserService userService;

    @Mock
    private AppUserKYCService kycService;

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private DriverLocationRepository driverLocationRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void testCancelDelivery_Success() {
        Long deliveryId = 1L;
        Delivery delivery = new Delivery();
        delivery.setId(deliveryId);
        delivery.setStatus(DeliveryStatus.REQUESTED);

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        adminService.cancelDelivery(deliveryId, "Customer request");

        verify(deliveryRepository).save(delivery);
        assertEquals(DeliveryStatus.CANCELLED, delivery.getStatus());
    }

    @Test
    void testCancelDelivery_DeliveryNotFound() {
        Long deliveryId = 1L;
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            adminService.cancelDelivery(deliveryId, "Test reason");
        });
    }

    @Test
    void testCancelDelivery_AlreadyCompleted() {
        Long deliveryId = 1L;
        Delivery delivery = new Delivery();
        delivery.setId(deliveryId);
        delivery.setStatus(DeliveryStatus.DELIVERED);

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

        assertThrows(BadRequestException.class, () -> {
            adminService.cancelDelivery(deliveryId, "Test reason");
        });
    }

    @Test
    void testRefundDelivery_Success() {
        Long deliveryId = 1L;
        Delivery delivery = new Delivery();
        delivery.setId(deliveryId);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("100.00"));

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(paymentRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(payment));

        adminService.refundDelivery(deliveryId, new BigDecimal("50.00"), "Test refund");

        verify(paymentService).refund(eq(1L), eq(new BigDecimal("50.00")), eq("Test refund"));
    }

    @Test
    void testRefundDelivery_DeliveryNotFound() {
        Long deliveryId = 1L;
        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            adminService.refundDelivery(deliveryId, new BigDecimal("50.00"), "Test refund");
        });
    }

    @Test
    void testRefundDelivery_NoCompletedPayment() {
        Long deliveryId = 1L;
        Delivery delivery = new Delivery();
        delivery.setId(deliveryId);

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(paymentRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> {
            adminService.refundDelivery(deliveryId, new BigDecimal("50.00"), "Test refund");
        });
    }

    @Test
    void testRefundDelivery_NegativeAmount() {
        Long deliveryId = 1L;
        Delivery delivery = new Delivery();
        delivery.setId(deliveryId);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("100.00"));

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(paymentRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> {
            adminService.refundDelivery(deliveryId, new BigDecimal("-50.00"), "Test refund");
        });
    }

    @Test
    void testRefundDelivery_AmountExceedsPayment() {
        Long deliveryId = 1L;
        Delivery delivery = new Delivery();
        delivery.setId(deliveryId);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setAmount(new BigDecimal("100.00"));

        when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
        when(paymentRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> {
            adminService.refundDelivery(deliveryId, new BigDecimal("150.00"), "Test refund");
        });
    }
}
