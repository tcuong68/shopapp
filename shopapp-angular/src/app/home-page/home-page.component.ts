import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../services/product.service';
import { ProductResponse , PagedData} from '../model/product.model';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss'],
  standalone: false
})
export class HomePageComponent implements OnInit {
  
  products: ProductResponse[] = [];
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;
  isLoading: boolean = false;
  error: string | null = null;

  constructor(
    private router: Router,
    private productService: ProductService
  ) {}

  ngOnInit(): void {
    this.initCarousel();
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.error = null;

    this.productService.getAllProducts(this.currentPage, this.pageSize).subscribe({
      next: (pagedData: PagedData<ProductResponse>) => {
        this.products = pagedData.content;
        this.totalPages = pagedData.totalPages;
        this.isLoading = false;
      },
      error: err => {
        console.error('Lỗi khi tải sản phẩm:', err);
        this.error = 'Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.';
        this.isLoading = false;
      }
    });
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadProducts();
  }


  

  navigateToProductDetail(productId: number): void {
    this.router.navigate(['/product/detail', productId]);
  }

  private initCarousel(): void {
    setTimeout(() => {
      const carousel = document.querySelector(".carousel") as HTMLElement;
      const slides = document.querySelectorAll(".carousel img");
      const totalSlides = slides.length;
      let index = 0;
      const intervalTime = 5000;

      if (!carousel || slides.length === 0) return;

      function slideShow() {
        index++;
        if (index >= totalSlides) {
          carousel.style.transition = "none";
          index = 0;
          carousel.style.transform = `translateX(0%)`;
          void carousel.offsetWidth;
          carousel.style.transition = "transform 0.5s ease-in-out";
        } else {
          carousel.style.transition = "transform 0.5s ease-in-out";
          carousel.style.transform = `translateX(${-index * 100}%)`;
        }
      }

      setInterval(slideShow, intervalTime);
    }, 500);
  }

  getProductImage(product: ProductResponse): string {
    if (product.imageUrls.length > 0) {
      return `http://localhost:8080/shopapp${product.imageUrls[0]}`;
    }
    return 'assets/default-image.jpg';
  }
}
