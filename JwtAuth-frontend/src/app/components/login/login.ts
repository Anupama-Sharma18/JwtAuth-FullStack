import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  imports: [FormsModule],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  
  username = '';
  password = '';

  message = '';
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login(): void {

    this.message = '';
    this.errorMessage = '';

    const loginRequest = {
      username: this.username,
      password: this.password
    };


    this.authService
      .login(loginRequest)
      .subscribe({

        next: (response) => {

          this.authService
            .saveTokens(response);

          this.errorMessage = '';

          this.message =
            `Login successful. Welcome ${response.username}`;

          setTimeout(() => {

            this.router.navigate([
              '/dashboard'
            ]);

          }, 1000);
        },


        error: (error) => {

          console.error(
            'Login failed:',
            error
          );

          this.message = '';

          this.errorMessage =
            'Login failed. Invalid username or password.';
        }

      });
  }


  loginWithGoogle(): void {

    this.authService
      .loginWithGoogle();
  }
}