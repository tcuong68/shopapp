import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable, of } from 'rxjs';
import { HttpClient } from '@angular/common/http'; // <- thêm dòng này
export interface CartItemResponse {
  id: number;
  productId: number;
  variantId: number;
  productName: string;
  quantity: number;
  size?: string;
  color?: string;
  price: number;
  totalPrice: number;
}

export interface CartResponse {
  id: number;
  items: CartItemResponse[];
  subTotal: number;
}

export interface ApiResponse<T> {
  code: number;
  message?: string;
  result: T;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartItems: CartItemResponse[] = [];
  private cartSubject = new BehaviorSubject<CartItemResponse[]>([]);
  subtotal:number=0;
  constructor(private http: HttpClient) { // <- inject HttpClient ở đây
    this.loadCartFromStorage();
  }

  private loadCartFromStorage(): void {
    const storedCart = localStorage.getItem('cart');
    if (storedCart) {
      try {
        this.cartItems = JSON.parse(storedCart);
        this.cartSubject.next(this.cartItems);
      } catch (error) {
        console.error('Error parsing stored cart:', error);
        localStorage.removeItem('cart');
      }
    }
  }

  private saveCartToStorage(): void {
    localStorage.setItem('cart', JSON.stringify(this.cartItems));
  }

  getCartItems(): Observable<CartResponse> {
    return this.getCartFromBackend().pipe(
      map((cartResponse) => {
        this.cartItems = cartResponse.items;
        this.subtotal = cartResponse.subTotal; // ✅ GÁN lại subtotal từ backend
        this.cartSubject.next(this.cartItems); // cập nhật stream nếu cần
        this.saveCartToStorage();
        return cartResponse;
      })
    );
  }

  getCartFromBackend(): Observable<CartResponse> {
    return this.http.get<ApiResponse<CartResponse>>('http://localhost:8080/shopapp/cart')
      .pipe(
        map(response => response.result) // ✅ Trích xuất phần result chứa giỏ hàng
      );
  }
  

  // Gọi BE để thêm sản phẩm vào giỏ hàng
  addToCart(request: { productVariantId: number; quantity: number }): Observable<any> {
    return this.http.post('http://localhost:8080/shopapp/cart/add', request);
  }

  updateCartItem(cartItemId: number, quantity: number): Observable<any> {
    return this.http.put('http://localhost:8080/shopapp/cart/update', {
      cartItemId,
      quantity
    });
  }

  updateQuantity(index: number, quantity: number): void {
    if (index >= 0 && index < this.cartItems.length) {
      this.cartItems[index].quantity = quantity;
      this.cartSubject.next(this.cartItems);
      this.saveCartToStorage();
    }
  }

  removeCartItem(cartItemId: number): Observable<any> {
    return this.http.delete(`http://localhost:8080/shopapp/cart/delete/${cartItemId}`);
  }

  clearCart(): void {
    this.cartItems = [];
    this.cartSubject.next(this.cartItems);
    localStorage.removeItem('cart');
  }

  getSubtotal(): number {
    return this.subtotal;
  }

  getTotal(shippingFee: number = 0): number {
    return this.getSubtotal() + shippingFee;
  }

  getItemCount(): number {
    return this.cartItems.reduce((count, item) => count + item.quantity, 0);
  }
  getCheckoutInfo(): Observable<any> {
    const data = localStorage.getItem('checkout');
    if (data) {
      return of(JSON.parse(data));
    }
    return of(null);
  }

}
