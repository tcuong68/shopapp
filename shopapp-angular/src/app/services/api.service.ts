import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserSearchParams } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // Authentication APIs
  login(username: string, password: string): Observable<any> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
  
    return this.http.post(
      `${this.baseUrl}/auth/login`,
      { username, password },
      // { headers }
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/users/register`, userData);
  }

  // Product APIs
  getProducts(params?: any): Observable<any> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.http.get(`${this.baseUrl}/products`, { params: httpParams });
  }

  getProductById(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/products/${id}`);
  }

  // Admin - User Management APIs
  getUsers(): Observable<any> {
    this.setAuthHeader();
    return this.http.get(`${this.baseUrl}/users`);
  }

  getUserById(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/users/${id}`);
  }

  searchUsers(params: UserSearchParams): Observable<any> {
    this.setAuthHeader();
    return this.http.post(`${this.baseUrl}/users/search`, params);  }

  createUser(userData: any): Observable<any> {
    this.setAuthHeader();
    return this.http.post(`${this.baseUrl}/users/register`, userData);
  }

  updateUser(id: string, userData: any): Observable<any> {
    this.setAuthHeader();
    return this.http.put(`${this.baseUrl}/users/${id}`, userData);
  }

  deleteUser(id: number): Observable<any> {
    this.setAuthHeader();
    return this.http.delete(`${this.baseUrl}/users/${id}`);
  }

  deleteManyUsers(ids: number[]): Observable<any> {
    this.setAuthHeader();
    return this.http.delete(`${this.baseUrl}/admin/users`, { body: { ids } });
  }

  // Admin - Product Management APIs
  getAdminProducts(params?: any): Observable<any> {
    this.setAuthHeader();
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.http.get(`${this.baseUrl}/admin/products`, { params: httpParams });
  }

  createProduct(productData: any): Observable<any> {
    this.setAuthHeader();
    return this.http.post(`${this.baseUrl}/admin/products`, productData);
  }

  updateProduct(id: number, productData: any): Observable<any> {
    this.setAuthHeader();
    return this.http.put(`${this.baseUrl}/admin/products/${id}`, productData);
  }

  deleteProduct(id: number): Observable<any> {
    this.setAuthHeader();
    return this.http.delete(`${this.baseUrl}/admin/products/${id}`);
  }

  deleteManyProducts(ids: number[]): Observable<any> {
    this.setAuthHeader();
    return this.http.delete(`${this.baseUrl}/admin/products`, { body: { ids } });
  }

  // Order APIs
  createOrder(orderData: any): Observable<any> {
    this.setAuthHeader();
    return this.http.post(`${this.baseUrl}/order/checkout`, orderData);
  }

  getUserOrders(): Observable<any> {
    this.setAuthHeader();
    return this.http.get(`${this.baseUrl}/orders/user`);
  }

  getAllOrders(): Observable<any> {
    this.setAuthHeader();
    return this.http.get(`${this.baseUrl}/order/admin`);
  }

  // Helper methods
  private setAuthHeader(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }
} 