package com.taskhub.repository;

import com.taskhub.domain.Comment;
import com.taskhub.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByTaskOrderByCreatedAtAsc(Task task);
}