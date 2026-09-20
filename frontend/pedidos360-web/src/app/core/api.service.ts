import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Ot {
  id: string;
  clienteId: string;
  patente: string;
  descripcion: string | null;
  total: number;
  createdAt: string;
  updatedAt: string | null;
}

export interface OtInput {
  clienteId: string;
  patente: string;
  descripcion: string;
  total: number;
}

export interface OtItem {
  id: number;
  otId: string;
  concepto: string;
  cantidad: number;
  precioUnit: number;
  subtotal: number;
  createdAt: string;
}

export interface OtItemInput {
  concepto: string;
  cantidad: number;
  precioUnit: number;
}

export interface OtEvent {
  id: number;
  otId: string | null;
  eventType: string;
  payloadJson: string;
  createdAt: string;
}

export interface NotifyLog {
  id: number;
  otId: string | null;
  clienteId: string | null;
  canal: string | null;
  payloadJson: string;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/bff`;

  getOts(): Observable<Ot[]> {
    return this.http.get<Ot[]>(`${this.baseUrl}/ots`);
  }

  createOt(ot: OtInput): Observable<Ot> {
    return this.http.post<Ot>(`${this.baseUrl}/ots`, ot);
  }

  getOtItems(otId: string): Observable<OtItem[]> {
    return this.http.get<OtItem[]>(`${this.baseUrl}/ots/${otId}/items`);
  }

  createOtItem(otId: string, item: OtItemInput): Observable<OtItem> {
    return this.http.post<OtItem>(`${this.baseUrl}/ots/${otId}/items`, item);
  }

  getEvents(): Observable<OtEvent[]> {
    return this.http.get<OtEvent[]>(`${this.baseUrl}/events`);
  }

  getNotifications(): Observable<NotifyLog[]> {
    return this.http.get<NotifyLog[]>(`${this.baseUrl}/notifications`);
  }
}
