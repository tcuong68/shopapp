import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse, ProductResponse, PagedData } from '../model/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:8080/shopapp/products';

  constructor(private http: HttpClient) {}

  getAllProducts(
    page = 0,
    size = 10,
    slug?: string
  ): Observable<PagedData<ProductResponse>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (slug) {
      params = params.set('slug', slug);
    }

    return this.http
      .get<ApiResponse<PagedData<ProductResponse>>>(
        `${this.apiUrl}?page=${page}&size=${size}`, {params}
      )
      .pipe(map(res => res.result));
  }

  searchProducts(request:any,
    page = 0,
    size = 10,
  ): Observable<PagedData<ProductResponse>> {

   
     let params = new HttpParams()
       .set('page', page.toString())
       .set('size', size.toString());
      // Assuming sortBy and sortDir are also query params for POST /search, add if needed
      // .set('sortBy', 'id') // Example, adjust if needed
      // .set('sortDir', 'asc'); // Example, adjust if needed

    // Call the POST endpoint
    return this.http
      .post<ApiResponse<PagedData<ProductResponse>>>(`${this.apiUrl}/search`, request, { params })
      .pipe(map(res => res.result));
  }

  getProductById(productId: number): Observable<ProductResponse> {
    return this.http
      .get<ApiResponse<ProductResponse>>(`${this.apiUrl}/detail/${productId}`)
      .pipe(map(res => res.result));
  }
  deleteProduct(productId: number): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.apiUrl}/${productId}`)
      .pipe(map(res => res.result));
  }

  deleteManyProducts(selectedIds: number[]): Observable<void> {
    return this.http
      .post<ApiResponse<void>>(`${this.apiUrl}/deleteMany`, selectedIds)
      .pipe(map(res => res.result));
  }

  
  updateProduct(id: number, productData: any, images: File[]): Observable<any> {
    const formData = new FormData();
    formData.append('product', new Blob([JSON.stringify(productData)], { type: 'application/json' }));
    images.forEach(image => formData.append('images', image));
    return this.http.put(`${this.apiUrl}/${id}`, formData);
  }

  createProduct(productData: any, images: File[]): Observable<ProductResponse> {
    // const formData = new FormData();
    // formData.append('product', new Blob([JSON.stringify(productData)], { type: 'application/json' }));
    // images.forEach((file, idx) => {
    //   formData.append('images', file);
    // });
    // return this.http.post<ApiResponse<ProductResponse>>(`${this.apiUrl}`, formData).pipe(map(res => res.result));
    const formData = new FormData();

    // Chuyển productData thành Blob JSON để gửi dưới dạng multipart/form-data
    formData.append('product', new Blob([JSON.stringify(productData)], { type: 'application/json' }));
  
    // Gửi từng file ảnh với key là 'images'
    images.forEach(file => {
      formData.append('images', file);
    });
  
    // Gửi request POST multipart/form-data tới backend
    return this.http.post<ApiResponse<ProductResponse>>(`${this.apiUrl}`, formData)
      .pipe(map(res => res.result));
  }
}