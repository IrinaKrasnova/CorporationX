package faang.school.postservice.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.dto.comment.CommentDto;
import faang.school.postservice.event.NewCommentEvent;
import faang.school.postservice.exception.ListenerException;
import faang.school.postservice.mapper.comment.CommentMapper;
import faang.school.postservice.service.redis.comment.CommentCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NewCommentListener {

    private final CommentCacheService commentService;
    private final ObjectMapper objectMapper;
    private final CommentMapper commentMapper;

    public NewCommentListener(CommentCacheService commentService, ObjectMapper objectMapper, CommentMapper commentMapper) {
        this.commentService = commentService;
        this.objectMapper = objectMapper;
        this.commentMapper = commentMapper;
    }

    @KafkaListener(topics = "${spring.data.channel.new_comment.name}", groupId = "${spring.data.kafka.group-id}")
    public void listen(String event) {

        try {
            NewCommentEvent newCommentEvent = objectMapper.readValue(event, NewCommentEvent.class);
            log.info("Received new newCommentEvent {}", event);

            CommentDto dto = commentMapper.toDto(newCommentEvent);
            commentService.addCommentToPost(dto);
        } catch (JsonProcessingException e) {
            log.error("Error processing event JSON: {}", event, e);
            throw new SerializationException(e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while processing event: {}", event, e);
            throw new ListenerException(e.getMessage());
        }
    }
}