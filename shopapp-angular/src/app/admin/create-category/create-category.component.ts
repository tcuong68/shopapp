import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CategoryService } from '../../services/category.service';
import { CategoryRequest, CategoryResponse } from '../../model/category.model';

@Component({
  selector: 'app-create-category',
  templateUrl: './create-category.component.html',
  styleUrls: ['./create-category.component.scss'],
  standalone : false
})
export class CreateCategoryComponent implements OnInit {
  categoryForm: FormGroup;
  isEditMode = false;
  categoryId: number | null = null;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private categoryService: CategoryService
  ) {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      slug: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.categoryId = Number(this.route.snapshot.paramMap.get('id'));
    this.isEditMode = !!this.categoryId;

    if (this.isEditMode) {
      this.loadCategory();
    }
  }

  loadCategory(): void {
    this.categoryService.getCategoryById(this.categoryId!).subscribe({
      next: (category: CategoryResponse) => {
        this.categoryForm.patchValue({
          name: category.name,
          slug: category.slug,
          description: category.description
        });
      },
      error: () => {
        this.errorMessage = 'Không thể tải thông tin danh mục.';
      }
    });
  }

  saveCategory(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      this.errorMessage = 'Vui lòng nhập đầy đủ thông tin.';
      return;
    }

    const request: CategoryRequest = this.categoryForm.value;

    const request$ = this.isEditMode
      ? this.categoryService.updateCategory(this.categoryId!, request)
      : this.categoryService.createCategory(request);

    request$.subscribe({
      next: () => {
        this.successMessage = 'Danh mục đã được lưu!';
        setTimeout(() => this.router.navigate(['/admin/categories']), 1000);
      },
      error: () => {
        this.errorMessage = 'Lỗi khi lưu danh mục. Vui lòng thử lại.';
      }
    });
  }

  cancel(): void {
    if (confirm('Bạn có chắc chắn muốn hủy?')) {
      this.router.navigate(['/admin/categories']);
    }
  }
}