import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-restaurant-card',
  template: `
    <mat-card class="restaurant-card">
      <img mat-card-image [src]="restaurant.imageUrl || 'assets/default-restaurant.jpg'"
           [alt]="restaurant.name">
      <mat-card-content>
        <div class="card-header">
          <h3>{{restaurant.name}}</h3>
          <div class="rating">
            <mat-icon class="star">star</mat-icon>
            <span>{{restaurant.averageRating | number:'1.1-1'}}</span>
            <span class="rating-count">({{restaurant.totalRatings}})</span>
          </div>
        </div>
        <p class="description">{{restaurant.description}}</p>
        <div class="card-meta">
          <span class="meta-item">
            <mat-icon>schedule</mat-icon>
            {{restaurant.estimatedDeliveryTime || 30}} min
          </span>
          <span class="meta-item">
            <mat-icon>delivery_dining</mat-icon>
            {{restaurant.deliveryFee | currency}} delivery
          </span>
        </div>
        <div class="status-badge" [ngClass]="restaurant.status?.toLowerCase()">
          {{restaurant.status}}
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [``]
})
export class RestaurantCardComponent {
  @Input() restaurant: any;
}

