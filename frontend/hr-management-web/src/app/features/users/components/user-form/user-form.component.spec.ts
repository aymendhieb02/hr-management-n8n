import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserResponse } from '../../models/user.model';
import { UserFormComponent } from './user-form.component';

describe('UserFormComponent', () => {
  const manager: UserResponse = user(2, 'MANAGER');
  const hr: UserResponse = user(3, 'HR');
  const employee: UserResponse = user(4, 'EMPLOYEE');
  let fixture: ComponentFixture<UserFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [UserFormComponent] }).compileComponents();
    fixture = TestBed.createComponent(UserFormComponent);
    fixture.componentRef.setInput('users', [manager, hr, employee]);
    fixture.componentRef.setInput('departments', [{ id: 1, name: 'HR', description: null, createdAt: '', updatedAt: null }]);
    fixture.componentRef.setInput('positions', [{ id: 1, title: 'Engineer', description: null, createdAt: '', updatedAt: null }]);
    fixture.detectChanges();
  });

  it('requires core fields and creation password', () => {
    const save = fixture.nativeElement.querySelector('.primary-button') as HTMLButtonElement;

    expect(save.disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Password');
  });

  it('trims text, lowercases email, and includes password on create', () => {
    const saveSpy = vi.spyOn(fixture.componentInstance.save, 'emit');
    setValue(0, '  new.user  ');
    setValue(1, '  NEW.USER@TEST.COM  ');
    setValue(2, 'password1');
    setValue(3, '  New  ');
    setValue(4, '  User  ');

    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));

    expect(saveSpy).toHaveBeenCalledWith(expect.objectContaining({
      username: 'new.user',
      email: 'new.user@test.com',
      password: 'password1',
      firstName: 'New',
      lastName: 'User'
    }));
  });

  it('does not send password on edit and excludes invalid manager options', () => {
    fixture.componentRef.setInput('user', manager);
    fixture.detectChanges();
    const saveSpy = vi.spyOn(fixture.componentInstance.save, 'emit');

    expect(fixture.nativeElement.textContent).not.toContain('Password');
    expect(fixture.nativeElement.textContent).not.toContain('Employee User');
    expect(fixture.nativeElement.textContent).not.toContain('Manager User');
    expect(fixture.nativeElement.textContent).toContain('Hr User');

    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(saveSpy).toHaveBeenCalledWith(expect.not.objectContaining({ password: expect.anything() }));
  });

  function setValue(index: number, value: string): void {
    const input = fixture.nativeElement.querySelectorAll('input')[index] as HTMLInputElement;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function user(id: number, role: UserResponse['role']): UserResponse {
    const label = role.charAt(0) + role.slice(1).toLowerCase();
    return {
      id,
      username: role.toLowerCase(),
      email: `${role.toLowerCase()}@test.com`,
      firstName: label,
      lastName: 'User',
      phone: null,
      hireDate: null,
      role,
      status: 'ACTIVE',
      enabled: true,
      manager: null,
      department: null,
      position: null,
      createdAt: '',
      updatedAt: null
    };
  }
});
