import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService, User, UserSearchParams } from '../../services/user.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-user-management',
  standalone: false,
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  paginatedUsers: User[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalPages: number = 0;
  totalItems: number = 0;
  allSelected: boolean = false;
  loading = false;
  errorMessage = '';
  
  searchParams: UserSearchParams = {
    firstName: '',
    lastName: '',
    username: '',
    dob: '',
    location: '',
    role: ''
  };

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.userService.getUsers().subscribe({
      next: (data) => {
        this.loading = false;
        this.users = data.users.map(user => ({
          ...user,
          selected: false
        }));
        this.totalItems = data.totalItems;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.updatePaginatedUsers();
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading users:', error);
        this.errorMessage = 'Không thể tải danh sách người dùng. Vui lòng thử lại sau.';
      }
    });
  }

  updatePaginatedUsers(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.paginatedUsers = this.users.slice(startIndex, endIndex);
  }

  getPageArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedUsers();
    }
  }

  toggleSelectAll(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.allSelected = target.checked;
    
    this.users.forEach(user => {
      user.selected = this.allSelected;
    });
    
    this.updatePaginatedUsers();
  }

  toggleUserSelection(user: User): void {
    user.selected = !user.selected;
    this.allSelected = this.users.every(u => u.selected);
  }

  searchUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.userService.searchUsers(this.searchParams).subscribe({
      next: (data) => {
        this.loading = false;
        this.users = data.users.map(user => ({
          ...user,
          selected: false
        }));
        this.totalItems = data.totalItems;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.currentPage = 1;
        this.updatePaginatedUsers();
      },
      error: (error) => {
        this.loading = false;
        console.error('Error searching users:', error);
        this.errorMessage = 'Không thể tìm kiếm người dùng. Vui lòng thử lại sau.';
      }
    });
  }

  getUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    
    this.userService.getUsers().subscribe({
      next: (data) => {
        this.loading = false;
        this.users = data.users.map(user => ({
          ...user,
          selected: false
        }));
        this.totalItems = data.totalItems;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
        this.currentPage = 1;
        this.updatePaginatedUsers();
      },
      error: (error) => {
        this.loading = false;
        console.error('Error searching users:', error);
        this.errorMessage = 'Không thể tìm kiếm người dùng. Vui lòng thử lại sau.';
      }
    });
  }

  editUser(userId: number): void {
    // Chuyển hướng đến trang chỉnh sửa người dùng
    this.router.navigate(['/admin/users/edit', userId]);
  }

  deleteUser(userId: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa người dùng này?')) {
      this.loading = true;
      this.errorMessage = '';
      
      this.userService.deleteUser(userId).subscribe({
        next: () => {
          this.loading = false;
          // Cập nhật danh sách người dùng sau khi xóa
          this.users = this.users.filter(user => user.id !== userId);
          this.totalItems--;
          this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
          
          if (this.currentPage > this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages;
          }
          
          this.updatePaginatedUsers();
        },
        error: (error) => {
          this.loading = false;
          console.error('Error deleting user:', error);
          this.errorMessage = 'Không thể xóa người dùng. Vui lòng thử lại sau.';
        }
      });
    }
  }

  deleteSelectedUsers(): void {
    const selectedUsers = this.users.filter(user => user.selected);
    
    if (selectedUsers.length === 0) {
      alert('Vui lòng chọn ít nhất một người dùng để xóa');
      return;
    }
    
    if (confirm(`Bạn có chắc chắn muốn xóa ${selectedUsers.length} người dùng đã chọn?`)) {
      this.loading = true;
      this.errorMessage = '';
      
      const selectedIds = selectedUsers.map(user => user.id);
      
      this.userService.deleteManyUsers(selectedIds).subscribe({
        next: () => {
          this.loading = false;
          // Cập nhật danh sách người dùng sau khi xóa
          this.users = this.users.filter(user => !selectedIds.includes(user.id));
          this.totalItems -= selectedIds.length;
          this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
          
          if (this.currentPage > this.totalPages && this.totalPages > 0) {
            this.currentPage = this.totalPages;
          }
          
          this.updatePaginatedUsers();
        },
        error: (error) => {
          this.loading = false;
          console.error('Error deleting users:', error);
          this.errorMessage = 'Không thể xóa người dùng đã chọn. Vui lòng thử lại sau.';
        }
      });
    }
  }
}
