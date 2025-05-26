package hello.shoppingmall.board.repository;


import hello.shoppingmall.board.entity.Board;
import hello.shoppingmall.board.entity.BoardLike;
import hello.shoppingmall.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    Optional<BoardLike> findByBoardAndMember(Board board, Member member);
} 