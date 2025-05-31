import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { AdminRoutingModule } from './admin-routing.module';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { UserManagementComponent } from './user-management/user-management.component';
import { ProductManagementComponent } from './product-management/product-management.component';
import { OrderManagementComponent } from './order-management/order-management.component';
import { CreateUserComponent } from './create-user/create-user.component';
import { CreateProductComponent } from './create-product/create-product.component';
import { CategoryManagementComponent } from './category-management/category-management.component';
import { CreateCategoryComponent } from './create-category/create-category.component';


@NgModule({
  declarations: [
    AdminDashboardComponent,
    UserManagementComponent,
    ProductManagementComponent,
    OrderManagementComponent,
    CreateUserComponent,
    CreateProductComponent,
    CategoryManagementComponent,
    CreateCategoryComponent,
    CategoryManagementComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    AdminRoutingModule
  ]
})
export class AdminModule { }
