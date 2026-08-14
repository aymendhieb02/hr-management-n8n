import { Injectable } from '@angular/core';
import { LeaveRequestResponse } from '../../leaves/models/leave-request.model';
import { UserResponse } from '../../users/models/user.model';
import { CalendarEvent } from '../models/calendar-event.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CalendarService {
  toEvents(requests: LeaveRequestResponse[], users: UserResponse[] = []): CalendarEvent[] {
    const userDepartment = new Map(users.map((user) => [user.id, user.department?.name ?? null]));
    return requests.map((request) => ({
        leaveRequestId: request.id,
        userId: request.requester.id,
        employeeName: `${request.requester.firstName} ${request.requester.lastName}`,
        employeePhotoUrl: this.photoUrl(request.requester.photoUrl),
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
  private photoUrl(url:string|null|undefined):string|null{
    if(!url)return null;if(/^https?:\/\//i.test(url))return url;
    return `${environment.apiUrl.replace(/\/api\/?$/,'')}${url.startsWith('/')?'':'/'}${url}`;
  }
}
