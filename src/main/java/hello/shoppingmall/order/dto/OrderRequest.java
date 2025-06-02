package hello.shoppingmall.order.dto;

import hello.shoppingmall.member.entity.Member;
import hello.shoppingmall.product.dto.ProductRequest;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequest {
    private final Member member;
    private final List<ProductRequest> productList;

    public OrderRequest(Member member, List<ProductRequest> productList) {
        this.member = member;
        this.productList = productList;
    }
}
