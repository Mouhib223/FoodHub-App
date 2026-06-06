import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1`;

  constructor(private http: HttpClient) {}

  getCart(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/cart/${userId}`);
  }

  addToCart(userId: number, item: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/cart/${userId}/items`, item);
  }

  placeOrder(userId: number, orderData: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/orders/users/${userId}`, orderData);
  }

  getOrderById(orderId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/orders/${orderId}`);
  }

  getMyOrders(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/orders/users/${userId}`);
  }

  updateOrderStatus(orderId: number, status: string): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/orders/${orderId}/status?status=${status}`, {});
  }

  rateRestaurant(orderId: number, rating: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/orders/${orderId}/rate`, { rating });
  }
}

