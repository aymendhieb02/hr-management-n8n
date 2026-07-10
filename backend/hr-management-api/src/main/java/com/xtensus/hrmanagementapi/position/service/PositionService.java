package com.xtensus.hrmanagementapi.position.service;

import com.xtensus.hrmanagementapi.domain.entity.Position;
import com.xtensus.hrmanagementapi.position.dto.PositionRequest;
import com.xtensus.hrmanagementapi.position.dto.PositionResponse;
import com.xtensus.hrmanagementapi.position.exception.DuplicatePositionException;
import com.xtensus.hrmanagementapi.position.exception.PositionNotFoundException;
import com.xtensus.hrmanagementapi.position.mapper.PositionMapper;
import com.xtensus.hrmanagementapi.repository.PositionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    public PositionService(
            PositionRepository positionRepository,
            PositionMapper positionMapper
    ) {
        this.positionRepository = positionRepository;
        this.positionMapper = positionMapper;
    }

    @Transactional
    public PositionResponse create(PositionRequest request) {
        String title = normalizeTitle(request.getTitle());
        if (positionRepository.existsByTitleIgnoreCase(title)) {
            throw new DuplicatePositionException(title);
        }

        Position position = positionMapper.toEntity(request);
        position.setTitle(title);
        position.setCreatedAt(LocalDateTime.now());

        return positionMapper.toResponse(positionRepository.save(position));
    }

    @Transactional
    public PositionResponse update(Long id, PositionRequest request) {
        Position position = getPosition(id);
        String title = normalizeTitle(request.getTitle());
        if (positionRepository.existsByTitleIgnoreCaseAndIdNot(title, id)) {
            throw new DuplicatePositionException(title);
        }

        positionMapper.updateEntity(request, position);
        position.setTitle(title);
        position.setUpdatedAt(LocalDateTime.now());

        return positionMapper.toResponse(positionRepository.save(position));
    }

    @Transactional
    public void delete(Long id) {
        Position position = getPosition(id);
        positionRepository.delete(position);
    }

    @Transactional(readOnly = true)
    public PositionResponse findById(Long id) {
        return positionMapper.toResponse(getPosition(id));
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> findAll() {
        return positionRepository.findAll()
                .stream()
                .map(positionMapper::toResponse)
                .toList();
    }

    private Position getPosition(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));
    }

    private String normalizeTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Position title must not be blank");
        }

        return title.trim();
    }
}
