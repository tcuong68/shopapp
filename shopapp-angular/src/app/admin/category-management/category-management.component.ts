import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CategoryService } from '../../services/category.service';
import { CategoryResponse } from '../../model/product.model';

@Component({
  selector: 'app-category-management',
  templateUrl: './category-management.component.html',
  styleUrls: ['./category-management.component.scss'],
  standalone : false
})
export class CategoryManagementComponent implements OnInit {
  categories: CategoryResponse[] = [];
  paginatedCategories: CategoryResponse[] = [];
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 0;
  totalItems = 0;
  searchName = '';
  allSelected = false;
  loading = false;
  errorMessage = '';

  constructor(private categoryService: CategoryService, private router: Router) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.categoryService.getCategories().subscribe({
      next: data => {
        this.loading = false;
        this.categories = data.map(c => ({ ...c, selected: false }));
        this.totalItems = this.categories.length;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.updatePaginatedCategories();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = 'Không thể tải danh sách danh mục.';
      }
    });
  }
  searchCategories(): void {
    this.loading = true;
    this.categoryService.searchCategories(this.searchName).subscribe({
      next: data => {
        this.loading = false;
        this.categories = data.map(c => ({ ...c, selected: false }));
        this.totalItems = this.categories.length;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.currentPage = 1;
        this.updatePaginatedCategories();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = 'Không thể tìm kiếm danh mục.';
      }
    });
  }

  updatePaginatedCategories(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    this.paginatedCategories = this.categories.slice(start, start + this.itemsPerPage);
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedCategories();
    }
  }

  getPageArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  toggleSelectAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.allSelected = checked;
    this.categories.forEach(c => c.selected = checked);
    this.updatePaginatedCategories();
  }

  toggleCategorySelection(category: CategoryResponse): void {
    category.selected = !category.selected;
    this.allSelected = this.categories.every(c => c.selected);
  }

  editCategory(id: number): void {
    this.router.navigate(['/admin/categories/edit', id]);
  }

  deleteCategory(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
      this.categoryService.deleteCategory(id).subscribe(() => {
        this.categories = this.categories.filter(c => c.id !== id);
        this.totalItems--;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.updatePaginatedCategories();
      });
    }
  }

  deleteSelectedCategories(): void {
    const selectedIds = this.categories.filter(c => c.selected).map(c => c.id);
    if (selectedIds.length === 0) {
      alert('Vui lòng chọn ít nhất một danh mục để xóa');
      return;
    }
    if (confirm(`Bạn có chắc muốn xóa ${selectedIds.length} danh mục đã chọn?`)) {
      this.categoryService.deleteManyCategories(selectedIds).subscribe(() => {
        this.categories = this.categories.filter(c => !selectedIds.includes(c.id));
        this.totalItems = this.categories.length;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.updatePaginatedCategories();
      });
    }
  }
}