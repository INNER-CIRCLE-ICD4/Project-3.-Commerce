package innercircle.commerce.order.infra.adapter.persistence;

import innercircle.commerce.order.application.port.out.OrderRepositoryPort;
import innercircle.commerce.order.domain.model.aggregate.Order;
import innercircle.commerce.order.domain.model.vo.MemberId;
import innercircle.commerce.order.domain.model.vo.OrderId;
import innercircle.commerce.order.domain.model.vo.OrderNumber;
import innercircle.commerce.order.infra.adapter.persistence.entity.OrderEntity;
import innercircle.commerce.order.infra.adapter.persistence.mapper.OrderMapper;
import innercircle.commerce.order.infra.adapter.persistence.repository.SpringDataJpaOrderRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JpaOrderRepository
 * OrderRepository의 JPA 구현체
 */
@Repository
public class JpaOrderRepository implements OrderRepositoryPort {
    
    private final SpringDataJpaOrderRepository jpaRepository;
    private final OrderMapper orderMapper;

    public JpaOrderRepository(SpringDataJpaOrderRepository jpaRepository,
                            OrderMapper orderMapper) {
        this.jpaRepository = jpaRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity savedEntity = jpaRepository.save(entity);
        return orderMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.getValue())
                .map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNumber(OrderNumber orderNumber) {
        return jpaRepository.findByOrderNumber(orderNumber.getValue())
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByMemberId(MemberId memberId) {
        return jpaRepository.findByMemberId(memberId.getValue())
                .stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }


    @Override
    public List<Order> findByOrderedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByOrderedAtBetween(start, end)
                .stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(OrderId orderId) {
        return jpaRepository.existsById(orderId.getValue());
    }

    @Override
    public void deleteById(OrderId orderId) {
        jpaRepository.deleteById(orderId.getValue());
    }
}
