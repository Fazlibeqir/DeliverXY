package com.deliverXY.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@NoArgsConstructor
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Delivery delivery;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer", "handler"})
    private AppUser sender;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "message_type")
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;
    
    @Column(name = "media_url")
    private String mediaUrl;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum MessageType {
        TEXT, IMAGE, LOCATION, SYSTEM
    }
    
    public ChatMessage(Delivery delivery, AppUser sender, String message) {
        this.delivery = delivery;
        this.sender = sender;
        this.message = message;
    }
    
    public ChatMessage(Delivery delivery, AppUser sender, String message, MessageType messageType) {
        this.delivery = delivery;
        this.sender = sender;
        this.message = message;
        this.messageType = messageType;
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
} 