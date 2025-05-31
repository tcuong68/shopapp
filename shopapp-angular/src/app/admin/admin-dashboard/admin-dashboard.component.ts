import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: false,
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent {
  
  constructor(private router: Router) {}
  
  logout(): void {
    // Xóa thông tin đăng nhập
    localStorage.removeItem('adminToken');
    
    // Chuyển hướng về trang đăng nhập
    this.router.navigate(['/login']);
  }
}
