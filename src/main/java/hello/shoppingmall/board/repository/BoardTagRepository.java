package hello.shoppingmall.board.repository;

import hello.shoppingmall.board.entity.Board;
import hello.shoppingmall.board.entity.BoardTag;
import hello.shoppingmall.board.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface BoardTagRepository extends JpaRepository<BoardTag, Long> {
    boolean existsByBoardAndTag(Board board, Tag tag);

    @Query("SELECT bt.tag, COUNT(bt) as tagCount " +
           "FROM BoardTag bt " +
           "GROUP BY bt.tag " +
           "ORDER BY tagCount DESC " +
           "LIMIT :limit")
    List<Map<String, Object>> findPopularTags(@Param("limit") int limit);

    @Query("SELECT t FROM Tag t " +
           "JOIN BoardTag bt ON bt.tag = t " +
           "WHERE bt.board.id = :boardId " +
           "GROUP BY t " +
           "ORDER BY COUNT(bt) DESC")
    List<Tag> findTagsByBoardId(@Param("boardId") Long boardId);
} 