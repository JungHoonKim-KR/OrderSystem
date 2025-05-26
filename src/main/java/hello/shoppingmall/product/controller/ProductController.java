package hello.shoppingmall.product.controller;

import hello.shoppingmall.product.dto.ProductResponse;
import hello.shoppingmall.product.entity.ProductCategory;
import hello.shoppingmall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chapter3/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @RequestParam ProductCategory category,
            Pageable pageable) {
        return ResponseEntity.ok(productService.findProductsByCategory(category, pageable));
    }
} 