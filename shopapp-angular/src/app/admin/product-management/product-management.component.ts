import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { ProductResponse,  } from '../../model/product.model';
import { CategoryService } from '../../services/category.service';
import { CategoryResponse } from '../../model/product.model';
@Component({
  selector: 'app-product-management',
  standalone: false,
  templateUrl: './product-management.component.html',
  styleUrl: './product-management.component.scss'
})
export class ProductManagementComponent implements OnInit {
  products: ProductResponse[] = [];
  paginatedProducts: ProductResponse[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalPages: number = 0;
  totalItems: number = 0;
  allSelected: boolean = false;
  loading = false;
  errorMessage = '';
  categories: CategoryResponse[] = [];
  
  searchRequest = {
    name: '',
    size: '',
    color: '',
    minPrice: null,
    maxPrice: null,
    category: ''
  };

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    this.productService.getAllProducts().subscribe({
      next: (data) => {
        this.loading = false;
        this.products = data.content.map(product => ({
          ...product,
          selected: false
        }));
        this.totalItems = data.totalElements;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.updatePaginatedProducts();
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading products:', error);
        this.errorMessage = 'Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.';
      }
    });
  }

  updatePaginatedProducts(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.paginatedProducts = this.products.slice(startIndex, endIndex);
  }

  getPageArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedProducts();
    }
  }

  toggleSelectAll(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.allSelected = target.checked;
    
    this.products.forEach(product => {
    //   product.selected = this.allSelected;
    });
    
    this.updatePaginatedProducts();
  }

  toggleProductSelection(product: ProductResponse): void {
    product.selected = !product.selected;
    this.allSelected = this.products.every(p => p.selected);
  }

  searchProducts(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.productService.searchProducts(this.searchRequest).subscribe({
      next: (data) => {
        this.loading = false;
        this.products = data.content.map(product => ({
          ...product,
          selected: false
        }));
        this.totalItems = data.totalElements;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.currentPage = 1;
        this.updatePaginatedProducts();
      },
      error: (error) => {
        this.loading = false;
        console.error('Error searching products:', error);
        this.errorMessage = 'Không thể tìm kiếm sản phẩm. Vui lòng thử lại sau.';
      }
    });
  }

  editProduct(id: number): void {
    this.router.navigate(['/admin/products/edit', id]);
  }

  deleteProduct(productId: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
      this.loading = true;
      this.errorMessage = '';
      
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.loading = false;
          // Cập nhật danh sách sản phẩm sau khi xóa
          this.products = this.products.filter(product => product.id !== productId);
          this.totalItems--;
          this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
          
          if (this.currentPage > this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages;
          }
          
          this.updatePaginatedProducts();
        },
        error: (error) => {
          this.loading = false;
          console.error('Error deleting product:', error);
          this.errorMessage = 'Không thể xóa sản phẩm. Vui lòng thử lại sau.';
        }
      });
    }
  }

  deleteSelectedProducts(): void {
    const selectedProducts = this.products.filter(product => product.selected);
    
    if (selectedProducts.length === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm để xóa');
      return;
    }
    
    if (confirm(`Bạn có chắc chắn muốn xóa ${selectedProducts.length} sản phẩm đã chọn?`)) {
      this.loading = true;
      this.errorMessage = '';
      
      const selectedIds = selectedProducts.map(product => product.id);
      
      this.productService.deleteManyProducts(selectedIds).subscribe({
        next: () => {
          this.loading = false;
          // Cập nhật danh sách sản phẩm sau khi xóa
          this.products = this.products.filter(product => !selectedIds.includes(product.id));
          this.totalItems -= selectedIds.length;
          this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
          
          if (this.currentPage > this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages;
          }
          
          this.updatePaginatedProducts();
        },
        error: (error: any) => {
          this.loading = false;
          console.error('Error deleting products:', error);
          this.errorMessage = 'Không thể xóa sản phẩm đã chọn. Vui lòng thử lại sau.';
        }
      });
    }
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: () => {
        this.categories = [];
      }
    });
  }
}
