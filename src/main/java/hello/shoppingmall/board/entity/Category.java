package hello.shoppingmall.board.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    NOTICE("공지사항"),
    FREE("자유게시판"),
    QUESTION("질문게시판"),
    TECH("기술게시판");

    private final String title;
}