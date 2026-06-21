import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  
  isAuthenticated = this.authService.isAuthenticated;
  currentUser = this.authService.currentUser;

  deconnexion(): void {
    this.authService.deconnexion();
    this.router.navigate(['/connexion']);
  }
}
