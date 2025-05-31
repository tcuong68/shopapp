import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse, PagedData, CategoryResponse} from '../model/product.model'; // Assume model is shared or adjust as needed
import { CategoryRequest } from '../model/category.model';
@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = 'http://localhost:8080/shopapp/categories';

  constructor(private http: HttpClient) { }

  createCategory(req: CategoryRequest): Observable<CategoryResponse> {
    return this.http.post<ApiResponse<CategoryResponse>>(`${this.apiUrl}`, req)
    .pipe(map(res => res.result));;
  }
  
  updateCategory(id: number, req: CategoryRequest): Observable<CategoryResponse> {
    return this.http.put<CategoryResponse>(`${this.apiUrl}/${id}`, req);
  }
  
  getCategoryById(id: number): Observable<CategoryResponse> {
    return this.http.get<ApiResponse<CategoryResponse>>(`${this.apiUrl}/${id}`)
      .pipe(map(res => res.result));
  }

  getAllCategories(): Observable<CategoryResponse[]> {
    // Adjust page/size/sort as needed, or fetch all if the list is small
    // For now, fetching with default pagination and mapping to content
    return this.http.get<ApiResponse<PagedData<CategoryResponse>>>(this.apiUrl)
      .pipe(map(res => res.result.content));
  }
  getCategories(): Observable<CategoryResponse[]> {
    return this.http.get<ApiResponse<{ content: CategoryResponse[] }>>(this.apiUrl)
      .pipe(map(res => res.result.content));
  }

  searchCategories(name: string): Observable<CategoryResponse[]> {
    return this.http.get<ApiResponse<CategoryResponse[]>>(`${this.apiUrl}/search`, {
      params: new HttpParams().set('name', name)
    }).pipe(map(res => res.result));
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`).pipe(map(res => res.result));
  }

  deleteManyCategories(ids: number[]): Observable<void> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/deleteMany`, ids).pipe(map(res => res.result));
  }
} 