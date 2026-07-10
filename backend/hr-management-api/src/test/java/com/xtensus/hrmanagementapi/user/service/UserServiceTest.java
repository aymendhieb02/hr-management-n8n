package com.xtensus.hrmanagementapi.user.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.xtensus.hrmanagementapi.department.exception.DepartmentNotFoundException;
import com.xtensus.hrmanagementapi.domain.entity.Department;
import com.xtensus.hrmanagementapi.domain.entity.Position;
import com.xtensus.hrmanagementapi.domain.entity.User;
import com.xtensus.hrmanagementapi.domain.enums.RoleType;
import com.xtensus.hrmanagementapi.domain.enums.UserStatus;
import com.xtensus.hrmanagementapi.position.exception.PositionNotFoundException;
import com.xtensus.hrmanagementapi.repository.DepartmentRepository;
import com.xtensus.hrmanagementapi.repository.PositionRepository;
import com.xtensus.hrmanagementapi.repository.UserRepository;
import com.xtensus.hrmanagementapi.user.dto.PasswordUpdateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserCreateRequest;
import com.xtensus.hrmanagementapi.user.dto.UserResponse;
import com.xtensus.hrmanagementapi.user.dto.UserUpdateRequest;
import com.xtensus.hrmanagementapi.user.exception.InvalidManagerException;
import com.xtensus.hrmanagementapi.user.exception.UserNotFoundException;
import com.xtensus.hrmanagementapi.user.mapper.UserMapper;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PositionRepository positionRepository;

    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(
                userRepository,
                departmentRepository,
                positionRepository,
                new UserMapper(),
                passwordEncoder
        );
    }

    @Test
    void passwordIsStoredEncodedAndNotEqualToSubmittedPassword() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        userService.create(createRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        String passwordHash = captor.getValue().getPasswordHash();
        assertNotEquals("StrongPass123", passwordHash);
        assertTrue(passwordEncoder.matches("StrongPass123", passwordHash));
    }

    @Test
    void missingDepartmentReturnsNotFound() {
        UserCreateRequest request = createRequest();
        request.setDepartmentId(10L);
        when(departmentRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(DepartmentNotFoundException.class, () -> userService.create(request));
    }

    @Test
    void missingPositionReturnsNotFound() {
        UserCreateRequest request = createRequest();
        request.setPositionId(10L);
        when(positionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(PositionNotFoundException.class, () -> userService.create(request));
    }

    @Test
    void missingManagerReturnsNotFound() {
        UserCreateRequest request = createRequest();
        request.setManagerId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.create(request));
    }

    @Test
    void assigningEmployeeAsManagerReturnsBadRequest() {
        UserCreateRequest request = createRequest();
        request.setManagerId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, RoleType.EMPLOYEE)));

        assertThrows(InvalidManagerException.class, () -> userService.create(request));
    }

    @Test
    void userCannotBeTheirOwnManager() {
        UserUpdateRequest request = updateRequest();
        request.setManagerId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, RoleType.EMPLOYEE)));

        assertThrows(InvalidManagerException.class, () -> userService.update(1L, request));
    }

    @Test
    void circularManagerAssignmentReturnsBadRequest() {
        User currentUser = user(1L, RoleType.EMPLOYEE);
        User manager = user(2L, RoleType.MANAGER);
        manager.setManager(currentUser);
        UserUpdateRequest request = updateRequest();
        request.setManagerId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));

        assertThrows(InvalidManagerException.class, () -> userService.update(1L, request));
    }

    @Test
    void updateUserSuccessfully() {
        User existing = user(1L, RoleType.EMPLOYEE);
        Department department = department(3L);
        Position position = position(4L);
        UserUpdateRequest request = updateRequest();
        request.setEmail(" UPDATED@EXAMPLE.COM ");
        request.setDepartmentId(3L);
        request.setPositionId(4L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(3L)).thenReturn(Optional.of(department));
        when(positionRepository.findById(4L)).thenReturn(Optional.of(position));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.update(1L, request);

        assertTrue("updated@example.com".equals(response.getEmail()));
        assertTrue("Updated".equals(response.getFirstName()));
    }

    @Test
    void updatePasswordSuccessfully() {
        User existing = user(1L, RoleType.EMPLOYEE);
        existing.setPasswordHash("old-hash");
        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setNewPassword("NewStrongPass123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updatePassword(1L, request);

        assertNotEquals("NewStrongPass123", existing.getPasswordHash());
        assertTrue(passwordEncoder.matches("NewStrongPass123", existing.getPasswordHash()));
    }

    @Test
    void deleteUserSuccessfullyWhenNotReferenced() {
        User existing = user(1L, RoleType.EMPLOYEE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        userService.delete(1L);

        verify(userRepository).delete(existing);
    }

    private UserCreateRequest createRequest() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("jdoe");
        request.setEmail("jdoe@example.com");
        request.setPassword("StrongPass123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("123456789");
        request.setHireDate(LocalDate.of(2026, 1, 15));
        request.setRole(RoleType.EMPLOYEE);
        request.setStatus(UserStatus.ACTIVE);
        request.setEnabled(true);
        return request;
    }

    private UserUpdateRequest updateRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("updated");
        request.setEmail("updated@example.com");
        request.setFirstName("Updated");
        request.setLastName("User");
        request.setPhone("123456789");
        request.setHireDate(LocalDate.of(2026, 1, 15));
        request.setRole(RoleType.EMPLOYEE);
        request.setStatus(UserStatus.ACTIVE);
        request.setEnabled(true);
        return request;
    }

    private User user(Long id, RoleType role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setEmail("user" + id + "@example.com");
        user.setPasswordHash("hash");
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setEnabled(true);
        return user;
    }

    private Department department(Long id) {
        Department department = new Department();
        department.setId(id);
        department.setName("Department " + id);
        return department;
    }

    private Position position(Long id) {
        Position position = new Position();
        position.setId(id);
        position.setTitle("Position " + id);
        return position;
    }
}
