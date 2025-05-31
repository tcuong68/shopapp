import { Component, OnInit } from '@angular/core';
import { OrderService, Order as OrderModel } from '../../services/order.service';

@Component({
  selector: 'app-order-management',
  standalone: false,
  templateUrl: './order-management.component.html',
  styleUrl: './order-management.component.scss'
})
export class OrderManagementComponent implements OnInit {
  // Search filters
  searchOrderId: string = '';
  searchStatus: string = '';
  priceFrom: number | null = null;
  priceTo: number | null = null;
  orderDate: string | null = null;

  // Pagination
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 0;

  // Orders data
  orders: (OrderModel & { selected?: boolean })[] = [];
  loading: boolean = false;
  errorMessage: string = '';

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.fetchOrders();
  }

  fetchOrders(): void {
    this.loading = true;
    this.errorMessage = '';
  
    const request = {
      id: this.searchOrderId || null,
      status: this.searchStatus || null,
      minTotalPrice: this.priceFrom,
      maxTotalPrice: this.priceTo,
      orderDate: this.orderDate || null
    };
  
    this.orderService.searchOrders(request, this.currentPage - 1, this.itemsPerPage).subscribe({
      next: (data: any) => {
        const result = data?.result;
        if (Array.isArray(result?.content)) {
          this.orders = result.content;
          this.totalPages = result.totalPages;
        } else {
          this.orders = [];
          this.totalPages = 0;
        }
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Không thể tải danh sách đơn hàng.';
        console.error('Lỗi khi tải đơn hàng:', err);
      }
    });
  }
  searchOrders(): void {
    this.currentPage = 1;
    this.fetchOrders();
  }

  updateOrderStatus(order: OrderModel, newStatus: string): void {
    if (confirm(`Bạn có chắc chắn muốn chuyển đơn hàng ${order.id} sang trạng thái "${this.getStatusText(newStatus)}"?`)) {
      this.orderService.updateOrderStatus(order.id, newStatus).subscribe({
        next: (response) => {
          order.status = newStatus;
        },
        error: (err) => {
          console.error('Lỗi khi cập nhật trạng thái:', err);
          alert('Cập nhật trạng thái thất bại.');
        }
      });
    }
  }

  changePage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.fetchOrders();
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  toggleSelectAll(event: Event): void {
    const isChecked = (event.target as HTMLInputElement).checked;
    this.orders.forEach(order => order.selected = isChecked);
  }

  hasSelectedOrders(): boolean {
    return this.orders.some(order => order.selected);
  }

  deleteSelectedOrders(): void {
    if (confirm('Bạn có chắc chắn muốn xóa các đơn hàng đã chọn?')) {
      const selectedIds = this.orders.filter(o => o.selected).map(o => o.id);
      this.orders = this.orders.filter(o => !selectedIds.includes(o.id));
    }
  }

  deleteOrder(order: OrderModel & { selected?: boolean }): void {
    if (confirm(`Bạn có chắc chắn muốn xóa đơn hàng ${order.id}?`)) {
      this.orderService.deleteOrder(order.id).subscribe({
        next: () => {
          this.orders = this.orders.filter(o => o.id !== order.id);
          alert(`Đã xóa đơn hàng ${order.id} thành công.`);
        },
        error: err => {
          console.error('Xóa đơn hàng thất bại:', err);
          alert('Xóa đơn hàng thất bại. Vui lòng thử lại.');
        }
      });
    }
  }

  editOrder(order: OrderModel & { selected?: boolean }): void {
    console.log('Edit order:', order);
  }

  approveOrder(order: OrderModel & { selected?: boolean }): void {
    if (confirm(`Bạn có chắc chắn muốn duyệt đơn hàng ${order.id}?`)) {
      const targetOrder = this.orders.find(o => o.id === order.id);
      if (targetOrder) {
        targetOrder.status = 'PROCESSING';
      }
    }
  }

  addOrder(): void {
    console.log('Add new order');
  }

  showEditButton(status: string): boolean {
    return ['PENDING', 'PROCESSING'].includes(status);
  }

  getStatusText(status: string): string {
    switch (status) {
      case 'PENDING': return 'Chờ xác nhận';
      case 'PROCESSING': return 'Đang xử lý';
      case 'SHIPPING': return 'Đang vận chuyển';
      case 'COMPLETED': return 'Đã hoàn thành';
      case 'CANCELLED': return 'Đã hủy';
      default: return status;
    }
  }
}
