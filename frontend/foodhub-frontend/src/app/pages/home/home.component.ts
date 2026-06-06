import { Component, OnInit } from '@angular/core';
import { RestaurantService } from '../../services/restaurant.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  template: `
    <div class="home-container">
      <!-- Hero Search -->
      <div class="hero-section">
        <h1>Hungry? We've got you.</h1>
        <p>Order from the best restaurants in your city</p>
        <div class="search-bar">
          <mat-form-field appearance="outline" class="search-field">
            <mat-icon matPrefix>search</mat-icon>
            <input matInput placeholder="Search restaurants or dishes..."
                   [(ngModel)]="searchQuery" (keyup.enter)="search()">
          </mat-form-field>
          <button mat-flat-button color="primary" (click)="search()">Search</button>
        </div>
      </div>

      <!-- Category chips -->
      <div class="categories">
        <mat-chip-listbox>
          <mat-chip-option *ngFor="let cat of categories" (click)="filterByCategory(cat)">
            {{cat.icon}} {{cat.name}}
          </mat-chip-option>
        </mat-chip-listbox>
      </div>

      <!-- Restaurant Grid -->
      <section class="restaurants-section">
        <h2>🔥 Popular Restaurants</h2>
        <div class="restaurant-grid">
          <app-restaurant-card
            *ngFor="let r of restaurants"
            [restaurant]="r"
            (click)="viewRestaurant(r.id)">
          </app-restaurant-card>
        </div>
        <mat-spinner *ngIf="loading" diameter="40"></mat-spinner>
      </section>
    </div>
  `,
  styles: [``]
})
export class HomeComponent implements OnInit {
  restaurants: any[] = [];
  loading = true;
  searchQuery = '';
  categories = [
    { name: 'Pizza', icon: '🍕' }, { name: 'Burgers', icon: '🍔' },
    { name: 'Sushi', icon: '🍱' }, { name: 'Salads', icon: '🥗' },
    { name: 'Desserts', icon: '🍰' }, { name: 'Asian', icon: '🍜' },
  ];

  constructor(private restaurantService: RestaurantService, private router: Router) {}

  ngOnInit(): void {
    this.restaurantService.getAllRestaurants().subscribe({
      next: (data) => { this.restaurants = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  search(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/restaurants'], { queryParams: { q: this.searchQuery } });
    }
  }

  filterByCategory(cat: any): void {
    this.router.navigate(['/restaurants'], { queryParams: { category: cat.name } });
  }

  viewRestaurant(id: number): void {
    this.router.navigate(['/restaurants', id]);
  }
}

