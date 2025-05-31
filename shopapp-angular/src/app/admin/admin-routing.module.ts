import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { ProductManagementComponent } from './product-management/product-management.component';
import { OrderManagementComponent } from './order-management/order-management.component';
import { CreateUserComponent } from './create-user/create-user.component';
import { CreateProductComponent } from './create-product/create-product.component';
import { CategoryManagementComponent } from './category-management/category-management.component';
import { CreateCategoryComponent } from './create-category/create-category.component';

const routes: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
    children: [
      { path: '', redirectTo: 'products', pathMatch: 'full' },
      { path: 'users', component: UserManagementComponent },
      { path: 'products', component: ProductManagementComponent },
      { path: 'orders', component: OrderManagementComponent },
      { path: 'users/create', component: CreateUserComponent },
      { path: 'users/edit/:id', component: CreateUserComponent },
      { path: 'products/edit/:id', component: CreateProductComponent }, 
      { path: 'products/create', component: CreateProductComponent },
      { path: 'categories', component: CategoryManagementComponent },
      { path: 'categories/create', component: CreateCategoryComponent },
      { path: 'categories/edit/:id', component: CreateCategoryComponent }

    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
