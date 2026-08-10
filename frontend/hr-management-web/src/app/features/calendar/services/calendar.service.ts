import { Injectable } from '@angular/core';
import { LeaveRequestResponse } from '../../leaves/models/leave-request.model';
import { UserResponse } from '../../users/models/user.model';
import { CalendarEvent } from '../models/calendar-event.model';

@Injectable({ providedIn: 'root' })
export class CalendarService {
  toEvents(requests: LeaveRequestResponse[], users: UserResponse[] = []): CalendarEvent[] {
    const userDepartment = new Map(users.map((user) => [user.id, user.department?.name ?? null]));
    return requests.map((request) => ({
        leaveRequestId: request.id,
        userId: request.requester.id,
        employeeName: `${request.requester.firstName} ${request.requester.lastName}`,
        leaveTypeName: request.leaveType.name,
        departmentName: userDepartment.get(request.requester.id) ?? null,
        startDate: request.startDate,
        endDate: request.endDate,
        status: request.status,
        nature: request.nature,
        startTime: request.startTime,
        endTime: request.endTime,
        reason: request.reason
      }));
  }
}
