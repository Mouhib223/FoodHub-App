import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RestaurantService {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/restaurants`;

  constructor(private http: HttpClient) {}

  getAllRestaurants(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getRestaurantById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  searchRestaurants(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/search?q=${query}`);
  }

  getMenu(restaurantId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/${restaurantId}/menu`);
  }

  createRestaurant(data: any): Observable<any> {
    return this.http.post<any>(this.baseUrl, data);
  }

  updateRestaurant(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}`, data);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.patch<any>(`${this.baseUrl}/${id}/status?status=${status}`, {});
  }
}

