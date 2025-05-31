import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CartService } from '../services/cart.service';
import { OrderService, OrderData } from '../services/order.service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss'],
  standalone: false
})
export class CheckoutComponent implements OnInit {
  checkoutForm: FormGroup;
  orderData: any = {};
  loading = false;
  errorMessage = '';
  successMessage = '';

  paymentMethods = [
    { id: 'cod', name: 'Thanh toán khi nhận hàng (COD)' },
    { id: 'vnpay', name: 'VNPAY' }
  ];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private cartService: CartService,
    private orderService: OrderService,
    private authService: AuthService
  ) {
    this.checkoutForm = this.fb.group({
      customerName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\d{10,11}$/)]],
      address: ['', Validators.required],
      note: [''],
      paymentMethod: ['cod', Validators.required]
    });
    
  }

  ngOnInit(): void {
    // Lấy thông tin từ cartService
    this.cartService.getCheckoutInfo().subscribe(data => {
      if (data) {
        this.orderData = data;
      } else {
        this.router.navigate(['/cart']);
      }
    });

    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.checkoutForm.patchValue({
        customerName: currentUser.username,
        customerEmail: currentUser.email
      });
    }
  }
 // checkout.component.ts - Fixed version
onSubmit(): void {
  if (this.checkoutForm.invalid) {
    this.markFormGroupTouched(this.checkoutForm);
    this.errorMessage = 'Vui lòng điền đầy đủ thông tin.';
    return;
  }

  this.loading = true;
  const formData = this.checkoutForm.value;

  const orderData = {
    phoneNumber: formData.phoneNumber,
    email: formData.email,
    address: formData.address,
    note: formData.note,
    paymentMethod: formData.paymentMethod
  };

  console.log('Dữ liệu gửi đi:', orderData);
  this.orderService.createOrder(orderData).subscribe({
    next: (response) => {
      console.log('Create Order Response:', response);
      
      // Backend trả về ApiResponse<OrderResponse>
      // Cấu trúc: { result: { id: number, ... }, code: 1000, message: "..." }
      const orderId = response.result?.id;
      
      if (!orderId) {
        console.error('Không tìm thấy orderId trong response:', response);
        this.errorMessage = 'Không thể lấy ID đơn hàng từ server.';
        this.loading = false;
        return;
      }

      console.log('Order ID:', orderId);

      if (formData.paymentMethod === 'vnpay') {
        // Gọi API tạo VNPAY URL với orderId vừa tạo
        this.orderService.createVnpayUrl(orderId).subscribe({
          next: (vnpayRes) => {
            console.log('VNPAY Response:', vnpayRes);
            
            // VNPAY response cũng có cấu trúc ApiResponse
            if (vnpayRes.result?.paymentUrl) {
              window.location.href = vnpayRes.result.paymentUrl;
            } else {
              console.error('Không tìm thấy paymentUrl:', vnpayRes);
              this.errorMessage = 'Không lấy được URL thanh toán từ VNPAY.';
              this.loading = false;
            }
          },
          error: (error) => {
            console.error('VNPAY Error:', error);
            this.errorMessage = 'Lỗi khi kết nối VNPAY.';
            this.loading = false;
          }
        });
      } else {
        // COD - thanh toán khi nhận hàng
        this.successMessage = 'Đặt hàng thành công!';
        this.cartService.clearCart();
        this.loading = false;
        setTimeout(() => this.router.navigate(['/']), 2000);
      }
    },
    error: (error) => {
      console.error('Create Order Error:', error);
      this.errorMessage = 'Có lỗi xảy ra khi tạo đơn hàng.';
      this.loading = false;
    }
  });
}
  
  
  markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
    });
  }
}
