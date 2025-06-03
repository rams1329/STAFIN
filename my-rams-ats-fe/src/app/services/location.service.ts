import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export interface Location {
  id: number;
  name: string;
  description?: string;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private baseUrl = `${environment.apiUrl}/api/locations`;
  private publicUrl = `${environment.apiUrl}/api/public/locations`;

  constructor(private http: HttpClient) {}

  // Public method to get active locations (no authentication required)
  getPublicActiveLocations(): Observable<Location[]> {
    return this.http.get<any>(`${this.publicUrl}/active`)
      .pipe(
        map(response => {
          // Handle ApiResponse wrapper
          if (response.data && Array.isArray(response.data)) {
            return response.data.map((location: any) => ({
              id: location.id,
              name: location.name,
              description: location.description,
              isActive: location.isActive
            }));
          }
          return [];
        })
      );
  }

  getLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(this.baseUrl);
  }

  getActiveLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(`${this.baseUrl}/active`);
  }

  getLocationById(id: number): Observable<Location> {
    return this.http.get<Location>(`${this.baseUrl}/${id}`);
  }

  createLocation(location: Partial<Location>): Observable<Location> {
    return this.http.post<Location>(this.baseUrl, location);
  }

  updateLocation(id: number, location: Partial<Location>): Observable<Location> {
    return this.http.put<Location>(`${this.baseUrl}/${id}`, location);
  }

  deleteLocation(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  toggleLocationStatus(id: number): Observable<Location> {
    return this.http.patch<Location>(`${this.baseUrl}/${id}/toggle-active`, {});
  }
} 