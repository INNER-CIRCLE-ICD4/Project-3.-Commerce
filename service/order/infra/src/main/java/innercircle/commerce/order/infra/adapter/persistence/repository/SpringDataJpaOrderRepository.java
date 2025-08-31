package innercircle.commerce.order.infra.adapter.persistence.repository;

import innercircle.commerce.order.infra.adapter.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SpringDataJpaOrderRepository
 * Spring Data JPA를 사용하는 Repository 인터페이스
 */
public interface SpringDataJpaOrderRepository extends JpaRepository<OrderEntity, Long> {

    /**
     * 주문번호로 주문 조회
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.orderNumber = :orderNumber")
    Optional<OrderEntity> findByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 회원 ID로 주문 목록 조회 (DDL: member_id)
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.memberId = :memberId ORDER BY o.orderDate DESC")
    List<OrderEntity> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 기간별 주문 조회 (DDL: order_date)
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.orderDate BETWEEN :start AND :end ORDER BY o.orderDate DESC")
    List<OrderEntity> findByOrderedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 삭제되지 않은 주문 존재 여부 확인
     */
    @Query("SELECT COUNT(o) > 0 FROM OrderEntity o WHERE o.id = :orderId")
    boolean existsByIdAndNotDeleted(@Param("orderId") Long orderId);
}
