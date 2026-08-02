package com.xtensus.hrmanagementapi.leave.accrual.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LeaveAccrualSchedulerTest {

    @Test
    void schedulerDisabledMeansNoExecution() {
        LeaveAccrualService service = Mockito.mock(LeaveAccrualService.class);
        LeaveAccrualProperties properties = new LeaveAccrualProperties();
        properties.setEnabled(false);
        LeaveAccrualScheduler scheduler = new LeaveAccrualScheduler(service, properties);

        scheduler.runMonthlyAccrual();

        verify(service, never()).run(null, null);
    }
}
