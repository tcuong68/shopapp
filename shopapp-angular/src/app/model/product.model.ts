// product.model.ts
export interface ProductResponse {
    id: number;
    name: string;
    description: string;
    price: number;
    imageUrls: string[];
    variants: Variant[];
    categoryId: number;
    selected: boolean;
    categoryName: string;
  }
  
  export interface Variant {
    id: number;
    size: string;
    color: string;
    price: number;
    quantity: number;
  }

  export interface PagedData<T> {
    content: T[];
    pageNo: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
  }
  
  export interface ApiResponse<T> {
    code: number;
    message: string;
    result: T;
  }

  export interface CategoryResponse {
    id: number;
    name: string;
    slug: string;
    selected: boolean;
  }
  export interface ProductReview {
    id: number;
    comment: string;
    rating: number;
    username: string | null;
    createdAt: string;
  }