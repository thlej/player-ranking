import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Player} from "../models/player";
import {Observable, of} from "rxjs";
import {catchError} from "rxjs/operators";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class PlayerService {

  baseApiUri = environment['PLAYERS_API_URI']

  constructor(protected httpClient: HttpClient) { }

  public getAllPlayers(): Observable<Player[]>{
    return this.httpClient.get<Player[]>(`${this.baseApiUri}/api/v1/players`)
      .pipe(
        catchError(err => {
          console.error(`Unexpected error occurs while fetching players data : ${JSON.stringify(err, null, 4)}`)
          return of([])
        })
      );
  }
}

