import { Component, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})

export class RegisterComponent {
  @ViewChild('registerForm') registerForm!: NgForm;

  user = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    dob: ''
  };

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    if (this.registerForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      this.successMessage = '';

      console.log("Đang gửi dữ liệu đăng ký:", this.user);

      this.authService.register(this.user).subscribe({
        next: response => {
          this.loading = false;
          console.log('Đăng ký thành công:', response);
          this.successMessage = 'Đăng ký thành công!';
          
          // Chuyển hướng đến trang đăng nhập sau 2 giây
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: error => {
          this.loading = false;
          console.error('Đăng ký thất bại:', error);
          
          if (error.status === 409) {
            this.errorMessage = 'Email hoặc tên đăng nhập đã tồn tại';
          } else {
            this.errorMessage = 'Lỗi đăng ký. Vui lòng thử lại.';
          }
        }
      });
    } else {
      this.errorMessage = "Vui lòng điền đầy đủ thông tin hợp lệ.";
    }
  }
}
