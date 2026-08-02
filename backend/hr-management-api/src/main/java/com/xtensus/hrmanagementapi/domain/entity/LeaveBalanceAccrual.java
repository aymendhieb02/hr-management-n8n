package com.xtensus.hrmanagementapi.domain.entity;

import com.xtensus.hrmanagementapi.domain.enums.LeaveAccrualStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "leave_balance_accruals",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_leave_balance_accruals_user_type_year_month",
                columnNames = {"user_id", "leave_type_id", "accrual_year", "accrual_month"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class LeaveBalanceAccrual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "accrual_year", nullable = false)
    private Integer accrualYear;

    @Column(name = "accrual_month", nullable = false)
    private Integer accrualMonth;

    @Column(name = "credited_days", nullable = false, precision = 5, scale = 2)
    private BigDecimal creditedDays;

    @Column(name = "balance_before", nullable = false, precision = 5, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 5, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LeaveAccrualStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
