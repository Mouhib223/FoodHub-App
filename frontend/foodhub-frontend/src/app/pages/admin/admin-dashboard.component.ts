import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-admin-dashboard',
  template: `
    <div class="admin-container">
      <h1>Admin Dashboard</h1>

      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-icon class="stat-icon users">people</mat-icon>
          <div>
            <h2>{{stats.users}}</h2>
            <p>Total Users</p>
          </div>
        </mat-card>
        <mat-card class="stat-card">
          <mat-icon class="stat-icon restaurants">restaurant</mat-icon>
          <div>
            <h2>{{stats.restaurants}}</h2>
            <p>Restaurants</p>
          </div>
        </mat-card>
        <mat-card class="stat-card">
          <mat-icon class="stat-icon orders">receipt</mat-icon>
          <div>
            <h2>{{stats.orders}}</h2>
            <p>Total Orders</p>
          </div>
        </mat-card>
        <mat-card class="stat-card">
          <mat-icon class="stat-icon drivers">delivery_dining</mat-icon>
          <div>
            <h2>{{stats.drivers}}</h2>
            <p>Active Drivers</p>
          </div>
        </mat-card>
      </div>

      <mat-tab-group>
        <mat-tab label="Users">
          <table mat-table [dataSource]="users" class="admin-table">
            <ng-container matColumnDef="id">
              <th mat-header-cell *matHeaderCellDef>ID</th>
              <td mat-cell *matCellDef="let u">{{u.id}}</td>
            </ng-container>
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Name</th>
              <td mat-cell *matCellDef="let u">{{u.firstName}} {{u.lastName}}</td>
            </ng-container>
            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef>Email</th>
              <td mat-cell *matCellDef="let u">{{u.email}}</td>
            </ng-container>
            <ng-container matColumnDef="role">
              <th mat-header-cell *matHeaderCellDef>Role</th>
              <td mat-cell *matCellDef="let u">
                <mat-chip>{{u.role}}</mat-chip>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="['id','name','email','role']"></tr>
            <tr mat-row *matRowDef="let row; columns: ['id','name','email','role'];"></tr>
          </table>
        </mat-tab>

        <mat-tab label="Restaurants">
          <table mat-table [dataSource]="restaurants" class="admin-table">
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Name</th>
              <td mat-cell *matCellDef="let r">{{r.name}}</td>
            </ng-container>
            <ng-container matColumnDef="city">
              <th mat-header-cell *matHeaderCellDef>City</th>
              <td mat-cell *matCellDef="let r">{{r.city}}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let r">
                <mat-select [value]="r.status" (selectionChange)="updateRestaurantStatus(r.id, $event.value)">
                  <mat-option value="PENDING">Pending</mat-option>
                  <mat-option value="ACTIVE">Active</mat-option>
                  <mat-option value="SUSPENDED">Suspended</mat-option>
                </mat-select>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="['name','city','status']"></tr>
            <tr mat-row *matRowDef="let row; columns: ['name','city','status'];"></tr>
          </table>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [``]
})
export class AdminDashboardComponent implements OnInit {
  stats = { users: 0, restaurants: 0, orders: 0, drivers: 0 };
  users: any[] = [];
  restaurants: any[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    const api = environment.apiUrl;
    this.http.get<any[]>(`${api}/api/v1/users`).subscribe(u => {
      this.users = u;
      this.stats.users = u.length;
    });
    this.http.get<any[]>(`${api}/api/v1/restaurants`).subscribe(r => {
      this.restaurants = r;
      this.stats.restaurants = r.length;
    });
  }

  updateRestaurantStatus(id: number, status: string): void {
    this.http.patch(`${environment.apiUrl}/api/v1/restaurants/${id}/status?status=${status}`, {})
      .subscribe(() => console.log('Status updated'));
  }
}

