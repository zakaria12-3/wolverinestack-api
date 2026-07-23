import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class JobService {

  private API = 'https://wolverinestack-api.onrender.com';

  constructor(private http: HttpClient) {}

  getJobs() {
    return this.http.get<any[]>(`${this.API}/candidate/jobs`);
  }
}
