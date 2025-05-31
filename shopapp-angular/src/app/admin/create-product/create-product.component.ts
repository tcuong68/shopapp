import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { CategoryResponse } from '../../model/product.model';

// Define interfaces for the form model
interface ProductVariant {
  size: string;
  color: string;
  price: number;
  quantity: number;
}

@Component({
  selector: 'app-create-product',
  standalone: false,
  templateUrl: './create-product.component.html',
  styleUrl: './create-product.component.scss'
})
export class CreateProductComponent implements OnInit {
  productForm: FormGroup;
  imagePreviewUrl: string| null = null;
  selectedFile: File | null = null;
  categories: CategoryResponse[] = [];
  productId: number | null = null;
  isEditMode = false;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private productService: ProductService,
    private categoryService: CategoryService
  ) {
    this.productForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      category: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(1)]],
      variants: this.fb.array([])
    });

    this.addVariant(); // Default variant
  }

  ngOnInit(): void {
    this.productId = Number(this.route.snapshot.paramMap.get('id'));
    this.isEditMode = !!this.productId;

    this.categoryService.getAllCategories().subscribe({
      next: (categories) => (this.categories = categories),
      error: () => (this.errorMessage = 'Không thể tải danh mục sản phẩm.')
    });

    if (this.isEditMode) {
      this.loadProduct();
    }
  }

  loadProduct(): void {
    this.productService.getProductById(this.productId!).subscribe({
      next: (product) => {
        this.productForm.patchValue({
          name: product.name,
          description: product.description,
          category: product.categoryId,
          price: product.price
        });

        // Remove default and set variants
        this.variantsArray.clear();
        product.variants.forEach((v: any) =>
          this.variantsArray.push(this.fb.group({
            size: [v.size, Validators.required],
            color: [v.color, Validators.required],
            price: [v.price, [Validators.required, Validators.min(1)]],
            quantity: [v.quantity, [Validators.required, Validators.min(0)]]
          }))
        );

        if (product.imageUrls && product.imageUrls.length > 0) {
          this.imagePreviewUrl = product.imageUrls[0]; // Lấy ảnh đầu tiên
        }
      },
      error: () => {
        this.errorMessage = 'Không thể tải sản phẩm.';
      }
    });
  }

  get variantsArray(): FormArray {
    return this.productForm.get('variants') as FormArray;
  }

  getVariantsControls() {
    return this.variantsArray.controls;
  }

  addVariant(): void {
    this.variantsArray.push(this.fb.group({
      size: ['', Validators.required],
      color: ['', Validators.required],
      price: [1, [Validators.required, Validators.min(1)]],
      quantity: [1, [Validators.required, Validators.min(1)]]
    }));
  }

  removeVariant(index: number): void {
    this.variantsArray.removeAt(index);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
      this.createImagePreview(this.selectedFile);
    }
  }

  createImagePreview(file: File): void {
    const reader = new FileReader();
    reader.onload = (e) => {
      this.imagePreviewUrl = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }

  cropImage(): void {
    console.log('Image cropping feature to be implemented');
  }

  saveProduct(): void {
    if (this.productForm.invalid) {
      this.markFormGroupTouched(this.productForm);
      this.errorMessage = 'Vui lòng điền đầy đủ thông tin yêu cầu.';
      return;
    }

    this.loading = true;
    const formValue = this.productForm.value;
    const productData = {
      name: formValue.name,
      description: formValue.description,
      categoryId: formValue.category,
      price: Math.round(formValue.price),
      variants: formValue.variants.map((v: any) => ({
        size: v.size,
        color: v.color,
        price: Math.round(v.price),
        quantity: Math.round(v.quantity)
      }))
    };

    const images: File[] = this.selectedFile ? [this.selectedFile] : [];

    const request$ = this.isEditMode
      ? this.productService.updateProduct(this.productId!, productData, images)
      : this.productService.createProduct(productData, images);
    console.log(this.productForm);

    request$.subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Sản phẩm đã được lưu!';
        setTimeout(() => this.router.navigate(['/admin/products']), 1000);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Lỗi khi lưu sản phẩm. Vui lòng thử lại.';
      }
    });
  }

  markFormGroupTouched(form: FormGroup | FormArray): void {
    Object.values(form.controls).forEach(control => {
      control.markAsTouched();
      if (control instanceof FormGroup || control instanceof FormArray) {
        this.markFormGroupTouched(control);
      }
    });
  }

  cancel(): void {
    if (confirm('Bạn có chắc chắn muốn hủy?')) {
      this.router.navigate(['/admin/products']);
    }
  }
}