package innercircle.commerce.product.infra.repository;

import innercircle.commerce.product.core.application.repository.ImageDeletionTargetRepository;
import innercircle.commerce.product.core.domain.ImageDeletionTarget;
import innercircle.commerce.product.infra.entity.ImageDeletionTargetJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ImageDeletionTargetRepositoryAdapter implements ImageDeletionTargetRepository {

    private final ImageDeletionTargetJpaRepository jpaRepository;

    @Override
    public ImageDeletionTarget save(ImageDeletionTarget target) {
        ImageDeletionTargetJpaEntity entity = ImageDeletionTargetJpaEntity.from(target);
        ImageDeletionTargetJpaEntity savedEntity = jpaRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public List<ImageDeletionTarget> saveAll(List<ImageDeletionTarget> targets) {
        List<ImageDeletionTargetJpaEntity> entities = targets.stream()
                .map(ImageDeletionTargetJpaEntity::from)
                .collect(Collectors.toList());
        List<ImageDeletionTargetJpaEntity> savedEntities = jpaRepository.saveAll(entities);
        return savedEntities.stream()
                .map(ImageDeletionTargetJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ImageDeletionTarget> findById(Long id) {
        return jpaRepository.findById(id)
                .map(ImageDeletionTargetJpaEntity::toDomain);
    }

    @Override
    public List<ImageDeletionTarget> findAllByScheduledDeletionAtBefore(LocalDateTime now, Pageable pageable) {
        List<ImageDeletionTargetJpaEntity> entities = jpaRepository.findAllByScheduledDeletionAtBefore(now, pageable);
        return entities.stream()
                .map(ImageDeletionTargetJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImageDeletionTarget> findAllByScheduledDeletionAtBefore(LocalDateTime now) {
        List<ImageDeletionTargetJpaEntity> entities = jpaRepository.findAllByScheduledDeletionAtBefore(now);
        return entities.stream()
                .map(ImageDeletionTargetJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImageDeletionTarget> findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(
            LocalDateTime now, int maxRetryCount, Pageable pageable) {
        List<ImageDeletionTargetJpaEntity> entities = jpaRepository
                .findAllByScheduledDeletionAtBeforeAndRetryCountLessThanEqual(now, maxRetryCount, pageable);
        return entities.stream()
                .map(ImageDeletionTargetJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(ImageDeletionTarget target) {
        jpaRepository.deleteById(target.getId());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll(List<ImageDeletionTarget> targets) {
        List<Long> ids = targets.stream()
                .map(ImageDeletionTarget::getId)
                .collect(Collectors.toList());
        jpaRepository.deleteAllById(ids);
    }

    @Override
    public boolean existsByImageUrl(String imageUrl) {
        return jpaRepository.existsByImageUrl(imageUrl);
    }

    @Override
    public Optional<ImageDeletionTarget> findByImageUrl(String imageUrl) {
        return jpaRepository.findByImageUrl(imageUrl)
                .map(ImageDeletionTargetJpaEntity::toDomain);
    }
}