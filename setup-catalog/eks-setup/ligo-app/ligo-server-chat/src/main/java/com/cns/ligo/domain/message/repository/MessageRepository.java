package com.cns.ligo.domain.message.repository;

import com.cns.ligo.domain.message.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    
    // Find all messages ordered by creation time
    List<MessageEntity> findAllByOrderByCreatedAtAsc();
    
    // Find messages by room ID
    List<MessageEntity> findByRoomIdOrderByCreatedAtAsc(String roomId);
    
    // Find recent messages with limit
    @Query(value = "SELECT * FROM chat_messages ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<MessageEntity> findRecentMessages(int limit);
}
