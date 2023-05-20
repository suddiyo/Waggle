package com.example.waggle.repository.board.boardtype;

import com.example.waggle.domain.board.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {

    //boardId를 통해 story 하나를 가져온다.
    @Query(value = "select * from story join board on board.board_id = :boardId", nativeQuery = true)
    Story findByBoardId(@Param("boardId") Long id);


    //hashtag를 통해 board를 모두 가져온다.(story에 한해서)
    @Query(value = "SELECT * FROM story AS s" +
            " JOIN board AS b ON s.board_id = b.board_id" +
            " JOIN board_hashtag AS bh ON b.board_id = bh.board_id" +
            " JOIN hashtag AS h ON bh.hashtag_id = h.hashtag_id" +
            " WHERE h.h_content = :content", nativeQuery = true)
    List<Story> findByHashtag(@Param("content") String content);
}