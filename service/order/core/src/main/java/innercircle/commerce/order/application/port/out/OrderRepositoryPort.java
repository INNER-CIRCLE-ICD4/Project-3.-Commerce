package innercircle.commerce.order.application.port.out;

import innercircle.commerce.order.domain.model.aggregate.Order;
import innercircle.commerce.order.domain.model.vo.MemberId;
import innercircle.commerce.order.domain.model.vo.OrderId;
import innercircle.commerce.order.domain.model.vo.OrderNumber;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderRepository Interface
 * 주문 저장소의 출력 포트
 * Infrastructure Layer에서 구현
 */
public interface OrderRepositoryPort {
    
    /**
     * 주문 저장
     */
    Order save(Order order);
    
    /**
     * ID로 주문 조회
     */
    Optional<Order> findById(OrderId orderId);
    
    /**
     * 주문번호로 주문 조회
     */
    Optional<Order> findByOrderNumber(OrderNumber orderNumber);
    
    /**
     * 회원 ID로 주문 목록 조회
     */
    List<Order> findByMemberId(MemberId memberId);
    
    /**
     * 기간별 주문 조회
     */
    List<Order> findByOrderedAtBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 주문 존재 여부 확인
     */
    boolean existsById(OrderId orderId);
    
    /**
     * 주문 삭제 (소프트 삭제 권장)
     */
    void deleteById(OrderId orderId);
}
