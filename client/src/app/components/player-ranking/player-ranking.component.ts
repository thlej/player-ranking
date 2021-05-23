import {Component, OnInit} from '@angular/core';
import {Player} from "../../models/player";
import {PlayerService} from "../../services/player.service";

@Component({
  selector: 'app-player-ranking',
  templateUrl: './player-ranking.component.html',
  styleUrls: ['./player-ranking.component.css']
})
export class PlayerRankingComponent implements OnInit {

  players: Player[] = [];

  constructor(protected playerService: PlayerService) {
  }

  ngOnInit(): void {
    this.playerService.getAllPlayers().subscribe(players => this.players = players)
  }

}
