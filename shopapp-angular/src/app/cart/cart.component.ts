import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService, CartItemResponse } from '../services/cart.service';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss'], // ✅ sửa lại thành styleUrls (dạng mảng)
  standalone: false
})
export class CartComponent implements OnInit {
  cartItems: CartItemResponse[] = [];
  shippingFee: number = 30000;
  orderNote: string = '';
  subtotal: number = 0;
  constructor(
    private router: Router,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.cartService.getCartItems().subscribe({
      next: (response) => {
        this.cartItems = response.items; // ✅ Sửa chỗ này
        this.subtotal = response.subTotal;
      },
      error: err => {
        // alert('Lỗi tải giỏ hàng. Vui lòng thử lại sau.');
        console.error(err);
      }
    });
  }

  increaseQuantity(index: number): void {
    const item = this.cartItems[index];
    const newQuantity = item.quantity + 1;

    this.cartService.updateCartItem(item.id, newQuantity).subscribe({
      next: () => {
        this.reloadCart(); // ✅ Gọi lại để cập nhật subtotal và cartItems
      },
      error: err => {
        console.error('Lỗi khi cập nhật số lượng:', err);
        alert('Không thể cập nhật số lượng. Vui lòng thử lại.');
      }
    });
  }

  decreaseQuantity(index: number): void {
    const item = this.cartItems[index];
    if (item.quantity > 1) {
      const newQuantity = item.quantity - 1;

      this.cartService.updateCartItem(item.id, newQuantity).subscribe({
        next: () => {
          this.reloadCart(); // ✅ Gọi lại để cập nhật subtotal và cartItems
        },
        error: err => {
          console.error('Lỗi khi cập nhật số lượng:', err);
          alert('Không thể cập nhật số lượng. Vui lòng thử lại.');
        }
      });
    }
  }

  removeItem(index: number): void {
    const item = this.cartItems[index];
    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) {
      this.cartService.removeCartItem(item.id).subscribe({
        next: () => {
          this.reloadCart();
        },
        error: err => {
          console.error('Lỗi khi xóa sản phẩm:', err);
          alert('Không thể xóa sản phẩm. Vui lòng thử lại.');
        }
      });
    }
  }

  getSubtotal(): number {
    return this.cartService.getSubtotal();
  }

  getTotal(): number {
    return this.cartService.getTotal(this.shippingFee);
  }

  reloadCart(): void {
    this.cartService.getCartItems().subscribe({
      next: (response) => {
        this.cartItems = response.items;
        this.subtotal = response.subTotal;
      },
      error: err => {
        console.error('Lỗi khi cập nhật giỏ hàng:', err);
      }
    });
  }

  checkout(): void {
    if (this.cartItems.length === 0) {
      alert('Giỏ hàng trống. Vui lòng thêm sản phẩm vào giỏ hàng trước khi thanh toán.');
      return;
    }

    localStorage.setItem('checkout', JSON.stringify({
      items: this.cartItems,
      subtotal: this.getSubtotal(),
      shippingFee: this.shippingFee,
      total: this.getTotal(),
      note: this.orderNote
    }));

    this.router.navigate(['/checkout']);
  }
}