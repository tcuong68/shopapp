import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, map } from 'rxjs'; // 👉 Thêm `map` từ 'rxjs'

export interface User {
  id: number;
  username: string;
  fullname: string;
  dob: string;
  location: string;
  roles:  Role[];
  selected?: boolean;
}

export interface Role {
  name: string;
  description: string;
}

export interface UserSearchParams {
  firstName?: string;
  lastName?: string;
  username?: string;
  dob?: string;
  location?: string;
  role?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private apiService: ApiService) { }

  getUserById(id: string): Observable<any> {
    return this.apiService.getUserById(id);
  }

  getUsers(): Observable<{ users: User[], totalItems: number }> {
    return this.apiService.getUsers().pipe(
      map(response => {
        const users = response.result.content.map((u: any) => ({
          id: u.id,
          username: u.username,
          fullname: `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim(),
          dob: u.dob,
          location: u.location,
          roles: u.roles
        }));
        const totalItems = response.result.totalElements;
        return { users, totalItems };
      })
    );
  }

  searchUsers(params: UserSearchParams): Observable<{ users: User[], totalItems: number }> {
    return this.apiService.searchUsers(params).pipe(
      map(response => {
        const users = response.result.content.map((u: any) => ({
          id: u.id,
          username: u.username,
          fullname: `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim(),
          dob: u.dob,
          location: u.location,
          role: u.role
        }));
        const totalItems = response.result.totalElements;
        return { users, totalItems };
      })
    );
  }

  createUser(userData: any): Observable<User> {
    return this.apiService.createUser(userData);
  }

  updateUser(id: string, userData: any): Observable<any> {
    return this.apiService.updateUser(id, userData);
  }

  deleteUser(id: number): Observable<any> {
    return this.apiService.deleteUser(id);
  }

  deleteManyUsers(ids: number[]): Observable<any> {
    return this.apiService.deleteManyUsers(ids);
  }
}