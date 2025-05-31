export interface CategoryRequest {
    name: string;
    slug: string;
    description?: string;
  }
  
  export interface CategoryResponse {
    id: number;
    name: string;
    slug: string;
    description?: string;
  }