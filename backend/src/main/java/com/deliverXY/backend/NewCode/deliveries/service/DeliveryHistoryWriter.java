package com.deliverXY.backend.NewCode.deliveries.service;

import com.deliverXY.backend.NewCode.deliveries.domain.Delivery;
import com.deliverXY.backend.NewCode.deliveries.domain.DeliveryHistory;
import com.deliverXY.backend.NewCode.deliveries.repository.DeliveryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryHistoryWriter {

    private final DeliveryHistoryRepository historyRepo;

    @Transactional
    public void logHistory(@NonNull Delivery delivery, String note, String changedBy) {
        DeliveryHistory history = new DeliveryHistory();
        history.setDelivery(delivery);
        history.setStatus(delivery.getStatus());
        history.setChangedBy(changedBy);
        history.setNote(note);
        historyRepo.save(history);
    }
}
