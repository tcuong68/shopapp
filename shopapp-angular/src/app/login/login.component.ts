import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  user = {
    username: '',
    password: ''
  };
  loading = false;
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    if (this.user.username && this.user.password) {
      this.loading = true;
      this.errorMessage = '';

      this.authService.login(this.user.username, this.user.password).subscribe({
        next: (response) => {
          this.loading = false;
          console.log('Đăng nhập thành công:', response);
          
          // Chuyển hướng đến trang chủ
          if(this.user.username === "admin"){
            this.router.navigate(['/admin']);
          }
          else{
            this.router.navigate(['/home-page']);
          }
         
        },
        error: (error) => {
          this.loading = false;
          console.error('Đăng nhập thất bại:', error);
          
          if (error.status === 401) {
            this.errorMessage = 'Email hoặc mật khẩu không chính xác';
          } else {
            this.errorMessage = 'Đăng nhập thất bại. Vui lòng thử lại sau.';
          }
        }
      });
    } else {
      this.errorMessage = 'Vui lòng nhập đầy đủ thông tin đăng nhập.';
    }
  }
}
