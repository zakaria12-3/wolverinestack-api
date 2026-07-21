import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class JobService {

  private API = 'http://localhost:8027';

  constructor(private http: HttpClient) {}

  getJobs() {
    return this.http.get<any[]>(`${this.API}/candidate/jobs`);
  }
}
