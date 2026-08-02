import { Component, EventEmitter, input, Output } from '@angular/core';
import { UserResponse } from '../../models/user.model';

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrl: '../../../shared/resource-page.scss'
})
export class UserDetailsComponent {
  readonly user = input.required<UserResponse>();
  @Output() readonly close = new EventEmitter<void>();
}
