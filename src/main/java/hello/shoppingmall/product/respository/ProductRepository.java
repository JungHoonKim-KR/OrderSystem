package hello.shoppingmall.product.respository;


import hello.shoppingmall.product.entity.Product;
import hello.shoppingmall.product.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE p.category = :category")
    Page<Product> findByCategory(@Param("category") ProductCategory category, Pageable pageable);
} 