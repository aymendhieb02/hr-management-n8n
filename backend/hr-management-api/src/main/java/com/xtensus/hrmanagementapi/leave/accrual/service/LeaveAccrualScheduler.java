package com.xtensus.hrmanagementapi.leave.accrual.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LeaveAccrualScheduler {

    private static final Logger log = LoggerFactory.getLogger(LeaveAccrualScheduler.class);

    private final LeaveAccrualService leaveAccrualService;
    private final LeaveAccrualProperties properties;

    public LeaveAccrualScheduler(LeaveAccrualService leaveAccrualService, LeaveAccrualProperties properties) {
        this.leaveAccrualService = leaveAccrualService;
        this.properties = properties;
    }

    // Default cron: second minute hour day-of-month month day-of-week = 01:00 on day 1 of every month.
    @Scheduled(cron = "${app.leave-accrual.cron}", zone = "${app.leave-accrual.zone}")
    public void runMonthlyAccrual() {
        if (!properties.isEnabled()) {
            log.info("Leave accrual scheduler skipped because it is disabled");
            return;
        }
        leaveAccrualService.run(null, null);
    }
}
