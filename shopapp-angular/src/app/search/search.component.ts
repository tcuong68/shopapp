import { Component, OnInit } from '@angular/core';
import { ProductService } from '../services/product.service';
import { ProductResponse, CategoryResponse } from '../model/product.model';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoryService } from '../services/category.service';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss'],
  standalone: false
})
export class SearchComponent implements OnInit {
  products: ProductResponse[] = [];
  categories: CategoryResponse[] = [];

  searchRequest = {
    name: '',
    size: '',
    color: '',
    minPrice: null,
    maxPrice: null,
    category: ''
  };

  pageNumber = 0;
  pageSize = 9;
  totalPages = 0;
  isLoading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.loadCategories();

    // Nếu có query param slug thì tự động set category
    this.route.queryParamMap.pipe(
      switchMap(params => {
        const slug = params.get('slug');
        if (slug) {
          this.searchRequest.category = slug;
        }
        this.isLoading = true;
        this.pageNumber = 0;
        return this.productService.searchProducts(this.searchRequest, this.pageNumber, this.pageSize);
      })
    ).subscribe({
      next: (pagedData) => {
        this.products = pagedData.content;
        this.totalPages = pagedData.totalPages;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Lỗi tải sản phẩm.';
        this.isLoading = false;
      }
    });
  }

  onSearch() {
    this.pageNumber = 0;
    this.isLoading = true;
    this.productService.searchProducts(this.searchRequest, this.pageNumber, this.pageSize).subscribe({
      next: (pagedData) => {
        this.products = pagedData.content;
        this.totalPages = pagedData.totalPages;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Lỗi khi tìm kiếm.';
        this.isLoading = false;
      }
    });
  }

  prevPage() {
    if (this.pageNumber > 0) {
      this.pageNumber--;
      this.searchWithCurrentFilters();
    }
  }

  nextPage() {
    if (this.pageNumber < this.totalPages - 1) {
      this.pageNumber++;
      this.searchWithCurrentFilters();
    }
  }

  searchWithCurrentFilters() {
    this.isLoading = true;
    this.productService.searchProducts(this.searchRequest, this.pageNumber, this.pageSize).subscribe({
      next: (pagedData) => {
        this.products = pagedData.content;
        this.totalPages = pagedData.totalPages;
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Lỗi khi tải trang.';
        this.isLoading = false;
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: () => {
        this.error = 'Lỗi tải danh mục.';
        this.isLoading = false;
      }
    });
  }

  navigateToProductDetail(productId: number): void {
    this.router.navigate(['/product/detail', productId]);
  }

  // New methods for pagination
  changePage(page: number): void {
    if (page >= 0 && page < this.totalPages && page !== this.pageNumber) {
      this.pageNumber = page;
      this.searchWithCurrentFilters();
    }
  }

  getPageArray(): number[] {
    const pagesToShow = 5; // Number of pages to show in pagination
    const pageArray: number[] = [];
    let startPage = Math.max(0, this.pageNumber - Math.floor(pagesToShow / 2));
    let endPage = Math.min(this.totalPages - 1, startPage + pagesToShow - 1);

    // Adjust startPage if endPage hits totalPages limit
    if (endPage - startPage + 1 < pagesToShow) {
        startPage = Math.max(0, endPage - pagesToShow + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pageArray.push(i);
    }
    return pageArray;
  }

  getProductImage(product: ProductResponse): string {
    if (product.imageUrls.length > 0) {
      return `http://localhost:8080/shopapp${product.imageUrls[0]}`;
    }
    return 'assets/default-image.jpg';
  }
}