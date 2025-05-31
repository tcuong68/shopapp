import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { CategoryService } from './category.service';
// import jwt_decode from 'jwt-decode';

interface User {
  id: string;
  username: string;
  email: string;
  role: string;
}


export interface DecodedToken {
  sub: string;
  roles: string[]; // hoặc role: string nếu backend trả khác
  exp: number;
  [key: string]: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  private readonly TOKEN_KEY = 'access token';
  constructor(
    private apiService: ApiService,
    private router: Router,
    private categoryService: CategoryService
  ) {
    this.loadUserFromStorage();
  }
  

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  // decodeToken(): DecodedToken | null {
  //   const token = this.getToken();
  //   if (!token) return null;

  //   try {
  //     return jwt_decode(token);
  //   } catch (e) {
  //     console.error('Lỗi giải mã token:', e);
  //     return null;
  //   }
  // }
    // isAdmin(): boolean {
    //   const decoded = this.decodeToken();
    //   return decoded?.roles?.includes('ROLE_ADMIN') || false;
    // }
    
    
  private loadUserFromStorage(): void {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        const user = JSON.parse(storedUser);
        this.currentUserSubject.next(user);
      } catch (error) {
        console.error('Error parsing stored user:', error);
        localStorage.removeItem('user');
      }
    }
  }

  login(username: string, password: string): Observable<any> {
    return this.apiService.login(username, password).pipe(
      tap(response => {
        console.log('Login response:', response);
        if (response.code === 1000 && response.result?.token) {
          localStorage.setItem('access token', response.result.token);
          this.currentUserSubject.next(response.result.user);
        }
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.apiService.register(userData);
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.currentUserSubject.value && !!localStorage.getItem('token');
  }

  // isAdmin(): boolean {
  //   const user = this.currentUserSubject.value;
  //   return !!user && user.role === 'ADMIN';
  // }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
} 