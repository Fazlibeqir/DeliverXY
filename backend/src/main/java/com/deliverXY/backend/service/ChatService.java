package com.deliverXY.backend.service;

import com.deliverXY.backend.models.ChatMessage;
import java.util.List;

public interface ChatService {
    ChatMessage sendMessage(Long deliveryId, Long senderId, String message);
    ChatMessage sendSystemMessage(Long deliveryId, String message);
    List<ChatMessage> getDeliveryChatHistory(Long deliveryId);
    List<ChatMessage> getUnreadMessages(Long deliveryId, Long userId);
    void markMessageAsRead(Long messageId);
    void markAllMessagesAsRead(Long deliveryId, Long userId);
    void deleteMessage(Long messageId, Long userId);
} 