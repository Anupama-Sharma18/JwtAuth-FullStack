import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  imports: [],
  selector: 'app-dashboard',
  styleUrl: './dashboard.css',
  templateUrl: './dashboard.html',
})
export class Dashboard {
   username: string | null = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.username = this.authService.getUsername();
  }

  logout(): void {

    const refreshToken =
      this.authService.getRefreshToken();

    if (!refreshToken) {
      this.authService.clearStorage();
      this.router.navigate(['/login']);
      return;
    }

    this.authService.logout(refreshToken).subscribe({

      next: () => {

        this.authService.clearStorage();

        this.router.navigate(['/login']);
      },

      error: () => {

        // Even if backend logout fails,
        // remove local tokens
        this.authService.clearStorage();

        this.router.navigate(['/login']);
      }
    });
  }

}
