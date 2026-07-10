package com.xtensus.hrmanagementapi.user.service;

import com.xtensus.hrmanagementapi.department.exception.DepartmentNotFoundException;
import com.xtensus.hrmanagementapi.domain.entity.Department;
import com.xtensus.hrmanagementapi.domain.entity.Position;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.position.exception.PositionNotFoundException;
import com.xtensus.hrmanagementapi.repository.DepartmentRepository;
import com.xtensus.hrmanagementapi.repository.PositionRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.user.dto.PasswordUpdateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserCreateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserResponse;
import com.xtensus.hrmanagementapi.user.dto.UserUpdateRequest;
import com.xtensus.hrmanagementapi.user.exception.DuplicateEmailException;
import com.xtensus.hrmanagementapi.user.exception.DuplicateUsernameException;
import com.xtensus.hrmanagementapi.user.exception.InvalidManagerException;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import com.xtensus.hrmanagementapi.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String username = normalizeRequired(request.getUsername(), "Username");
        String email = normalizeEmail(request.getEmail());
        ensureUsernameIsUnique(username, null);
        ensureEmailIsUnique(email, null);

        User manager = resolveManager(request.getManagerId(), null);
        Department department = resolveDepartment(request.getDepartmentId());
        Position position = resolvePosition(request.getPositionId());

        User user = userMapper.toEntity(request);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(normalizeRequired(request.getFirstName(), "First name"));
        user.setLastName(normalizeRequired(request.getLastName(), "Last name"));
        user.setManager(manager);
        user.setDepartment(department);
        user.setPosition(position);
        user.setCreatedAt(LocalDateTime.now());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getUser(id);
        String username = normalizeRequired(request.getUsername(), "Username");
        String email = normalizeEmail(request.getEmail());
        ensureUsernameIsUnique(username, id);
        ensureEmailIsUnique(email, id);

        User manager = resolveManager(request.getManagerId(), id);
        Department department = resolveDepartment(request.getDepartmentId());
        Position position = resolvePosition(request.getPositionId());

        userMapper.updateEntity(request, user);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(normalizeRequired(request.getFirstName(), "First name"));
        user.setLastName(normalizeRequired(request.getLastName(), "Last name"));
        user.setManager(manager);
        user.setDepartment(department);
        user.setPosition(position);
        user.setUpdatedAt(LocalDateTime.now());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updatePassword(Long id, PasswordUpdateRequest request) {
        User user = getUser(id);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toResponse(getUser(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findByRole(RoleType role) {
        return userRepository.findByRole(role)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException(departmentId);
        }

        return userRepository.findByDepartmentId(departmentId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findTeamMembers(Long managerId) {
        if (!userRepository.existsById(managerId)) {
            throw new UserNotFoundException(managerId);
        }

        return userRepository.findByManagerId(managerId)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        User user = getUser(id);
        userRepository.delete(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    private Position resolvePosition(Long positionId) {
        if (positionId == null) {
            return null;
        }

        return positionRepository.findById(positionId)
                .orElseThrow(() -> new PositionNotFoundException(positionId));
    }

    private User resolveManager(Long managerId, Long userId) {
        if (managerId == null) {
            return null;
        }

        if (userId != null && managerId.equals(userId)) {
            throw new InvalidManagerException("A user cannot be their own manager");
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new UserNotFoundException(managerId));

        if (!isManagerRole(manager.getRole())) {
            throw new InvalidManagerException("Selected manager must have role MANAGER, HR, or ADMIN");
        }

        validateManagerChain(manager, userId);
        return manager;
    }

    private void validateManagerChain(User manager, Long userId) {
        if (userId == null) {
            return;
        }

        Set<Long> visitedIds = new HashSet<>();
        User current = manager;
        while (current != null && current.getId() != null) {
            if (current.getId().equals(userId)) {
                throw new InvalidManagerException("Manager assignment would create a circular relationship");
            }

            if (!visitedIds.add(current.getId())) {
                throw new InvalidManagerException("Manager chain contains a circular relationship");
            }

            current = current.getManager();
        }
    }

    private boolean isManagerRole(RoleType role) {
        return role == RoleType.MANAGER || role == RoleType.HR || role == RoleType.ADMIN;
    }

    private void ensureUsernameIsUnique(String username, Long currentUserId) {
        boolean duplicateExists = currentUserId == null
                ? userRepository.existsByUsernameIgnoreCase(username)
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(username, currentUserId);

        if (duplicateExists) {
            throw new DuplicateUsernameException(username);
        }
    }

    private void ensureEmailIsUnique(String email, Long currentUserId) {
        boolean duplicateExists = currentUserId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId);

        if (duplicateExists) {
            throw new DuplicateEmailException(email);
        }
    }

    private String normalizeEmail(String email) {
        return normalizeRequired(email, "Email").toLowerCase();
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.trim();
    }
}
