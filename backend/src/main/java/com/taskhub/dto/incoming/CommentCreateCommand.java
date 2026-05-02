package com.taskhub.dto.incoming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateCommand {

    @NotBlank(message = "The comment content cannot be empty")
    @Size(max = 2000, message = "The comment cannot be longer than 2000 characters")
    private String content;
}