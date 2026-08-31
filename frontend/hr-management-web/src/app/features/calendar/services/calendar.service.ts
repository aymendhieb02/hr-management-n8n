import { Injectable } from '@angular/core';
import { LeaveRequestResponse } from '../../leaves/models/leave-request.model';
import { UserResponse } from '../../users/models/user.model';
import { CalendarEvent } from '../models/calendar-event.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CalendarService {
  toEvents(requests: LeaveRequestResponse[], users: UserResponse[] = []): CalendarEvent[] {
    const userDepartment = new Map(users.map((user) => [user.id, user.department?.name ?? null]));
    return requests.flatMap((request) => {
      const base:CalendarEvent={
        kind: request.requestedDays === 0.5 ? 'HALF_DAY' : 'LEAVE',
        eventId:`leave-${request.id}`,
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
        reason: request.reason,
        consumedDays:request.consumedDays,actualEndDate:request.actualEndDate,regularizedAt:request.regularizedAt,
        regularizedBy:request.regularizedBy?`${request.regularizedBy.firstName} ${request.regularizedBy.lastName}`:null,
        regularizationComment:request.regularizationComment
      };
      if(request.consumedDays==null||request.consumedDays>=request.requestedDays)return[base];
      const consumed={...base,endDate:request.actualEndDate??request.startDate};
      const restoredStart=request.actualEndDate?this.nextDate(request.actualEndDate):request.startDate;
      const restored:CalendarEvent={...base,eventId:`restored-${request.id}`,kind:'RESTORED',startDate:restoredStart,endDate:request.endDate,leaveTypeName:'Jours restitués'};
      return request.consumedDays===0?[restored]:[consumed,restored];
    });
  }
  private nextDate(value:string):string{const date=new Date(`${value}T12:00:00`);date.setDate(date.getDate()+1);return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;}
  private photoUrl(url:string|null|undefined):string|null{
    if(!url)return null;if(/^https?:\/\//i.test(url))return url;
    return `${environment.apiUrl.replace(/\/api\/?$/,'')}${url.startsWith('/')?'':'/'}${url}`;
  }
}
