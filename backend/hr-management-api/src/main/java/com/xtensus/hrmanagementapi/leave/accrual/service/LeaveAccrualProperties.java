package com.xtensus.hrmanagementapi.leave.accrual.service;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.leave-accrual")
@Getter
@Setter
public class LeaveAccrualProperties {

    private boolean enabled = true;
    private BigDecimal monthlyDays = new BigDecimal("2.16");
    private String cron = "0 0 1 1 * *";
    private String zone = "Africa/Tunis";
}
