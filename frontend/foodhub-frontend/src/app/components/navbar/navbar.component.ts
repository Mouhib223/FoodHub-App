import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  template: `
    <mat-toolbar class="navbar">
      <div class="navbar-brand" routerLink="/home">
        <span class="logo-icon">🍔</span>
        <span class="brand-name">FoodHub</span>
      </div>

      <div class="nav-links">
        <a mat-button routerLink="/home" routerLinkActive="active">Home</a>
        <a mat-button routerLink="/restaurants" routerLinkActive="active">Restaurants</a>
      </div>

      <span class="flex-spacer"></span>

      <div class="nav-actions">
        <button mat-icon-button routerLink="/cart">
          <mat-icon [matBadge]="cartCount" matBadgeColor="warn">shopping_cart</mat-icon>
        </button>

        <ng-container *ngIf="isLoggedIn; else loginBlock">
          <button mat-icon-button [matMenuTriggerFor]="userMenu">
            <mat-icon>account_circle</mat-icon>
          </button>
          <mat-menu #userMenu="matMenu">
            <button mat-menu-item routerLink="/profile">
              <mat-icon>person</mat-icon> Profile
            </button>
            <button mat-menu-item routerLink="/admin" *ngIf="isAdmin">
              <mat-icon>admin_panel_settings</mat-icon> Admin
            </button>
            <button mat-menu-item routerLink="/restaurant-dashboard" *ngIf="isRestaurantOwner">
              <mat-icon>restaurant</mat-icon> My Restaurant
            </button>
            <mat-divider></mat-divider>
            <button mat-menu-item (click)="logout()">
              <mat-icon>logout</mat-icon> Sign Out
            </button>
          </mat-menu>
        </ng-container>

        <ng-template #loginBlock>
          <button mat-flat-button color="primary" (click)="login()">Sign In</button>
        </ng-template>
      </div>
    </mat-toolbar>
  `,
  styles: [``]
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
  isAdmin = false;
  isRestaurantOwner = false;
  cartCount = 0;

  constructor(private keycloak: KeycloakService, private router: Router) {}

  async ngOnInit() {
    this.isLoggedIn = await this.keycloak.isLoggedIn();
    if (this.isLoggedIn) {
      const roles = this.keycloak.getUserRoles();
      this.isAdmin = roles.includes('ADMIN');
      this.isRestaurantOwner = roles.includes('RESTAURANT_OWNER');
    }
  }

  login() { this.keycloak.login(); }
  logout() { this.keycloak.logout(window.location.origin); }
}

