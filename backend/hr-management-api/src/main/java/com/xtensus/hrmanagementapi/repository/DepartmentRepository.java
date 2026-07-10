package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
