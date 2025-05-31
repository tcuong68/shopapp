import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-create-user',
  standalone: false,
  templateUrl: './create-user.component.html',
  styleUrl: './create-user.component.scss'
})
export class CreateUserComponent implements OnInit {
  userForm: FormGroup;
  loading = false;
  errorMessage = '';
  successMessage = '';
  isEditMode = false;
  userId: string | null = null;

  availableRoles = [
    { id: 'USER', name: 'Người dùng' },
    { id: 'ADMIN', name: 'Quản trị viên' }
  ];

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: [''], // Không bắt buộc khi edit
      confirmPassword: [''],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      dob: [''],
      location: [''],
      role: ['USER', Validators.required]
    }, {
      validators: this.passwordMatchValidator.bind(this)
    });
  }

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id');
    this.isEditMode = !!this.userId;

    if (this.isEditMode && this.userId) {
      this.loadUser(this.userId);
      // Khi edit, password không bắt buộc
      this.userForm.get('password')?.clearValidators();
      this.userForm.get('password')?.updateValueAndValidity();
      this.userForm.get('confirmPassword')?.clearValidators();
      this.userForm.get('confirmPassword')?.updateValueAndValidity();
    } else {
      // Khi tạo mới, password và confirmPassword bắt buộc
      this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
      this.userForm.get('confirmPassword')?.setValidators([Validators.required]);
      this.userForm.get('password')?.updateValueAndValidity();
      this.userForm.get('confirmPassword')?.updateValueAndValidity();
    }
  }
  loadUser(id: string): void {
    this.loading = true;
    this.userService.getUserById(id).subscribe({
      next: (response: any) => {
        console.log('API response:', response); // Debug log
        this.loading = false;
        
        // Truy cập đúng vào response.result thay vì response trực tiếp
        const user = response.result;
        
        if (!user) {
          this.errorMessage = 'Không tìm thấy thông tin người dùng.';
          return;
        }
        
        // Gán dữ liệu vào form
        const formData = {
          username: user.username,
          firstName: user.firstName,
          lastName: user.lastName,
          dob: user.dob ? user.dob.substring(0,10) : '', // đã đúng format yyyy-MM-dd
          location: user.location || '',
          role: user.roles && user.roles.length > 0 ? user.roles[0].name : 'USER'
        };
        
        console.log('Form data to patch:', formData); // Debug log
        this.userForm.patchValue(formData);
      },
      error: (error: any) => {
        console.error('Load user error:', error);
        this.loading = false;
        this.errorMessage = 'Không tải được thông tin người dùng.';
      }
    });
  }

  // Custom validator for password match - chỉ kiểm tra khi có nhập password
  passwordMatchValidator(formGroup: FormGroup) {
    const password = formGroup.get('password')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;
    if (!password && !confirmPassword) return null;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  saveUser() {
    this.onSubmit();
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const userData = { ...this.userForm.value };
      delete userData.confirmPassword;

      if (!userData.password) {
        delete userData.password; // Nếu password trống thì không gửi lên backend (khi edit)
      }

      if (this.isEditMode && this.userId) {
        this.userService.updateUser(this.userId, userData).subscribe({
          next: () => {
            this.loading = false;
            this.successMessage = 'Cập nhật người dùng thành công!';
            setTimeout(() => this.router.navigate(['/admin/users']), 1500);
          },
          error: (error) => {
            this.loading = false;
            this.handleError(error);
          }
        });
      } else {
        this.userService.createUser(userData).subscribe({
          next: () => {
            this.loading = false;
            this.successMessage = 'Người dùng đã được tạo thành công!';
            setTimeout(() => this.router.navigate(['/admin/users']), 1500);
          },
          error: (error) => {
            this.loading = false;
            this.handleError(error);
          }
        });
      }
    } else {
      this.markFormGroupTouched(this.userForm);
      this.errorMessage = 'Vui lòng điền đầy đủ thông tin yêu cầu.';
    }
  }

  private handleError(error: any) {
    console.error('Error:', error);
    if (error.status === 409) {
      this.errorMessage = 'Tên đăng nhập đã tồn tại.';
    } else {
      this.errorMessage = 'Lỗi xảy ra. Vui lòng thử lại.';
    }
  }

  markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  cancel(): void {
    if (confirm('Bạn có chắc chắn muốn hủy? Dữ liệu đã nhập sẽ bị mất.')) {
      this.router.navigate(['/admin/users']);
    }
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.userForm.get(controlName);
    return !!control?.touched && !!control?.hasError(errorName);
  }

  hasPasswordMismatch(): boolean {
    return !!this.userForm.hasError('passwordMismatch') &&
           !!this.userForm.get('confirmPassword')?.touched;
  }
}