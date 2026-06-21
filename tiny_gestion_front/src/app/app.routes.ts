import { Routes } from '@angular/router';
import { ConnexionComponent } from './components/connexion/connexion.component';
import { InscriptionComponent } from './components/inscription/inscription.component';
import { TableauDeBordComponent } from './components/tableau-de-bord/tableau-de-bord.component';
import { RecettesComponent } from './components/recettes/recettes.component';
import { DepensesComponent } from './components/depenses/depenses.component';
import { FicheLogementComponent } from './components/fiche-logement/fiche-logement.component';
import { BiensAmortissablesComponent } from './components/biens-amortissables/biens-amortissables.component';
import { JustificatifsComponent } from './components/justificatifs/justificatifs.component';
import { AdministrationComponent } from './components/administration/administration.component';
import { ChecklistsComponent } from './components/checklists/checklists.component';
import { BilanAnnuelComponent } from './components/bilan-annuel/bilan-annuel.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'connexion', component: ConnexionComponent },
  { path: 'inscription', component: InscriptionComponent },
  { path: 'tableau-de-bord', component: TableauDeBordComponent, canActivate: [authGuard] },
  { path: 'recettes', component: RecettesComponent, canActivate: [authGuard] },
  { path: 'depenses', component: DepensesComponent, canActivate: [authGuard] },
  { path: 'logement', component: FicheLogementComponent, canActivate: [authGuard] },
  { path: 'biens-amortissables', component: BiensAmortissablesComponent, canActivate: [authGuard] },
  { path: 'justificatifs', component: JustificatifsComponent, canActivate: [authGuard] },
  { path: 'checklists', component: ChecklistsComponent, canActivate: [authGuard] },
  { path: 'bilan-annuel', component: BilanAnnuelComponent, canActivate: [authGuard] },
  { path: 'administration', component: AdministrationComponent, canActivate: [authGuard] },
  { path: '', redirectTo: '/tableau-de-bord', pathMatch: 'full' },
  { path: '**', redirectTo: '/tableau-de-bord' }
];

