package com.cns.ligo.domain.message.service;

import com.cns.ligo.domain.message.model.MessageEntity;
import com.cns.ligo.domain.message.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collections;

@Service
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    
    // Save a new message to database
    @Transactional
    public MessageEntity saveMessage(String sender, String content, String messageType, String roomId) {
        MessageEntity message = new MessageEntity(sender, content, messageType, roomId);
        return messageRepository.save(message);
    }
    
    // Retrieve all messages
    public List<MessageEntity> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtAsc();
    }
    
    // Retrieve messages by room
    public List<MessageEntity> getMessagesByRoom(String roomId) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }
    
    // Retrieve recent messages
    public List<MessageEntity> getRecentMessages(int limit) {
        List<MessageEntity> messages = messageRepository.findRecentMessages(limit);
        Collections.reverse(messages);
        return messages;
    }
}
