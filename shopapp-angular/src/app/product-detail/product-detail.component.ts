import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService} from '../services/product.service';
import { CartService } from '../services/cart.service';
import { ProductResponse, Variant , ProductReview} from '../model/product.model';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-product-detail',
  standalone: false,
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent implements OnInit {
  product: ProductResponse | null = null;
  selectedImage: string | null = null;
  selectedSize: string = '';
  selectedColor: string = '';
  quantity: number = 1;
  loading = false;
  errorMessage = '';
  productReviews: ProductReview[]=[];
  selectedVariant!: Variant | null;
  review = {
    rating: null,
    comment: ''
  };
  averageRating: number = 0;
  availableColors: string[] = [];
  availableSizes: string[] = [];
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const productId = params.get('id');
      if (productId) {
        this.loadProduct(+productId);
      }
    });
  }
  submitReview() {
    if (!this.product) return;
  
    const reviewRequest = {
      productId: this.product.id,
      rating: this.review.rating,
      comment: this.review.comment
    };
  
    this.http.post('http://localhost:8080/shopapp/reviews', reviewRequest).subscribe({
      next: () => {
        alert('Đánh giá của bạn đã được gửi!');
        this.review = { rating: null, comment: '' };
        if (this.product) {
          this.loadProductReviews(this.product.id); // Load lại danh sách đánh giá
        }
      }
    });
  }

  getStarsArray(rating: number): number[] {
    return Array(rating).fill(0);
  }

  getStarsArrayForAverage(): number[] {
    return Array(Math.round(this.averageRating)).fill(0);
  }
  
  getProductImage(product: ProductResponse): string {
    if (product.imageUrls.length > 0) {
      return `http://localhost:8080/shopapp${product.imageUrls[0]}`;
    }
    return 'assets/default-image.jpg';
  }

  loadProductReviews(productId: number): void {
    this.http.get<any>(`http://localhost:8080/shopapp/reviews/product/${productId}`).subscribe({
      next: (response) => {
        this.productReviews = response.result;
  
        // Tính điểm đánh giá trung bình nếu có review
        if (this.productReviews.length > 0) {
          const total = this.productReviews.reduce((sum, review) => sum + review.rating, 0);
          this.averageRating = total / this.productReviews.length;
        } else {
          this.averageRating = 0;
        }
      },
      error: (err) => {
        console.error('Không thể tải đánh giá sản phẩm:', err);
        this.productReviews = [];
        this.averageRating = 0;
      }
    });
  }
  loadProduct(productId: number): void {
    this.loading = true;
    this.errorMessage = '';
  
    this.productService.getProductById(productId).subscribe({
      next: (data) => {
        this.product = data;
        this.loading = false;
  
        // ảnh
        this.selectedImage = this.product.imageUrls.length > 0
          ? 'http://localhost:8080' + this.product.imageUrls[0]
          : 'assets/default-image.jpg';
  
        // màu và size
        this.availableColors = [...new Set(this.product.variants.map(v => v.color))];
        this.availableSizes = [...new Set(this.product.variants.map(v => v.size))];
  
        // mặc định chọn giá trị đầu tiên
        this.selectedColor = this.availableColors[0];
        this.selectedSize = this.availableSizes[0];
        this.updateSelectedVariant();
        this.loadProductReviews(productId);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading product:', error);
        this.errorMessage = 'Không thể tải thông tin sản phẩm. Vui lòng thử lại sau.';
      }
    });
  }

  updateSelectedVariant(): void {
    if (!this.product) return;
  
    this.selectedVariant = this.product.variants.find(v =>
      v.color === this.selectedColor && v.size === this.selectedSize
    ) || null;
  }
  

  selectImage(imageUrl: string) {
    this.selectedImage = imageUrl;
  }

  increaseQuantity(): void {
    this.quantity++;
  }

  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }



  
  addToCart(): void {
    if (!this.selectedVariant) {
      this.errorMessage = 'Vui lòng chọn size và màu trước khi thêm vào giỏ hàng';
      return;
    }
    console.log('Selected Variant:', this.selectedVariant); 
    const requestBody = {
      productVariantId: this.selectedVariant.id, // <-- Phải có ID của variant
      quantity: this.quantity
    };
  
    this.cartService.addToCart(requestBody).subscribe({
      next: (res) => {
        alert('Đã thêm sản phẩm vào giỏ hàng');
      },
      error: (err) => {
        console.error('Lỗi khi thêm vào giỏ:', err);
        this.errorMessage = 'Không thể thêm vào giỏ hàng. Vui lòng thử lại.';
      }
    });
  }
} 