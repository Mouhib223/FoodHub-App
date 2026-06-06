import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { KeycloakAuthGuard, KeycloakService } from 'keycloak-angular';

@Injectable({ providedIn: 'root' })
export class AuthGuard extends KeycloakAuthGuard {

  constructor(override protected readonly router: Router,
              override protected readonly keycloak: KeycloakService) {
    super(router, keycloak);
  }

  async isAccessAllowed(route: ActivatedRouteSnapshot): Promise<boolean | UrlTree> {
    if (!this.authenticated) {
      await this.keycloak.login({ redirectUri: window.location.href });
    }

    const requiredRoles = route.data['roles'] as string[];
    if (!requiredRoles || requiredRoles.length === 0) return true;

    return requiredRoles.some(role => this.roles.includes(role));
  }
}

