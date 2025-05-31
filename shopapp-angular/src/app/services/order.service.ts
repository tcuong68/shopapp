import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ApiResponse, CartItemResponse } from './cart.service';
import { map } from 'rxjs/operators';

export interface OrderItem {
  productName: string;
  quantity: number;
  price: number;
  totalPrice: number;
  size?: string; // Nếu cần kích thước
}

export interface OrderData {
  phoneNumber: string;
  email: string;
  address: string;
  note?: string;
  paymentMethod: string;
}

export interface Order {
  id: number;
  payment: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  address: string;
  shippingFee: number;
  status: string;
  totalPrice: number;
  items: OrderItem[];
}



@Injectable({
  providedIn: 'root'
})
export class OrderService {
  constructor(
    private apiService: ApiService,
    private http: HttpClient
  ) {}

  createOrder(orderData: OrderData): Observable<ApiResponse<Order>> {
    return this.apiService.createOrder(orderData);
  }
  getUserOrders(): Observable<Order[]> {
    return this.apiService.getUserOrders();
  }

  getAllOrders(page: number = 0, size: number = 5): Observable<any> {
    return this.http.get(`http://localhost:8080/shopapp/order/admin?page=${page}&size=${size}`);
  }

  createVnpayUrl(orderId: number): Observable<ApiResponse<{paymentUrl: string}>> {
    return this.http.get<ApiResponse<{paymentUrl: string}>>(`http://localhost:8080/shopapp/api/v1/payments/vnpay/create?orderId=${orderId}`);
  }
  // Convert cart items to order items
  cartItemsToOrderItems(cartItems: CartItemResponse[]): OrderItem[] {
    return cartItems.map(item => ({
      productName: item.productName,
      quantity: item.quantity,
      price: item.price,
      size: item.size, // Nếu có size
      totalPrice: item.quantity * item.price
    }));
  }

  deleteOrder(orderId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`http://localhost:8080/shopapp/order/admin/delete/${orderId}`)
     .pipe(map(res => res.result));
  }

  updateOrderStatus(orderId: number, status: string) {
    return this.http.put(`http://localhost:8080/shopapp/order/update/${orderId}?status=${status}`, {});
  }

  searchOrders(request: any, page: number, size: number) {
    const params = {
      page,
      size,
      sortBy: 'id',
      sortDir: 'asc'
    };
    return this.http.post(`http://localhost:8080/shopapp/order/admin/search?page=${page}&size=${size}`, request, { params });
  }
}
