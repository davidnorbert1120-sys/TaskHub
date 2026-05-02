package com.taskhub.dto.outgoing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentItem {

    private Long id;
    private String content;
    private String authorUsername;
    private LocalDateTime createdAt;
}