import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  imports: [],
  selector: 'app-oauth2-callback',
  styleUrl: './oauth2-callback.css',
  templateUrl: './oauth2-callback.html',
})
export class Oauth2Callback implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}


  ngOnInit(): void {

    const hash =
      window.location.hash.substring(1);

    const params =
      new URLSearchParams(hash);


    const accessToken =
      params.get('accessToken');

    const refreshToken =
      params.get('refreshToken');

    const username =
      params.get('username');


    if (
      accessToken &&
      refreshToken &&
      username
    ) {

      this.authService.saveTokens({

        token: accessToken,

        refreshToken: refreshToken,

        username: username

      });


      // Remove tokens from URL
      window.history.replaceState(
        {},
        document.title,
        '/dashboard'
      );


      this.router.navigate([
        '/dashboard'
      ]);

    } else {

      this.router.navigate([
        '/login'
      ]);
    }
  }
}