package org.example.expert.domain.todo.repository;

import jakarta.persistence.Entity;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("select t from Todo t order by t.modifiedAt desc")
    @EntityGraph(attributePaths = {"user"}, type =  EntityGraph.EntityGraphType.LOAD)
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @Query("select t from Todo t where t.id = :todoId")
    @EntityGraph(attributePaths = {"user"}, type =  EntityGraph.EntityGraphType.LOAD)
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    int countById(Long todoId);
}
