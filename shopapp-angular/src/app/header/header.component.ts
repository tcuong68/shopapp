import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  // get isAdmin(): boolean {
  //   return this.authService.isAdmin();
  // }

  constructor(private authService: AuthService) {}

  getCurrentUser() {
    return JSON.parse(localStorage.getItem('currentUser') || 'null');
  }
  
  // isAdmin(): boolean {
  //   const user = this.getCurrentUser();
  //   return user?.roles?.includes('ROLE_ADMIN');
  // }
}
