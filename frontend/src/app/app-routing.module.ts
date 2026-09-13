import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomePageComponent } from './Components/Home/home-page/home-page.component';
import { AllQuizzesComponent } from './Components/Quizzes/All-quizzes/all-quizzes.component';
import { CreateQuizComponent } from './Components/Quizzes/create-quiz/create-quiz.component';
import { LoginComponent } from './Components/Auth/login/login.component';
import { RegisterComponent } from './Components/Auth/register/register.component';
import { ResetPasswordComponent } from './Components/Auth/reset-password/reset-password.component';
import { ForgotPasswordComponent } from './Components/Auth/forgot-password/forgot-password.component';
import { CreateSetOfFlashcardsComponent } from './Components/Flashcards/Create-set-of-flashcards/create-set-of-flashcards.component';
import { DisplayQuizComponent } from './Components/Quizzes/Display-quiz/display-quiz.component';
import { AuthGuard } from './Guard/auth.guard';
import { DisplayFlashcardsComponent } from './Components/Flashcards/display-flashcards/display-flashcards.component';
import { SearchFlashcardComponent } from './Components/Flashcards/search-flashcard/search-flashcard.component';
import { DisplaySetOfFlashcardsComponent } from './Components/Flashcards/display-set-of-flashcards/display-set-of-flashcards.component';
import { GameLobbyComponent } from './Components/Game/game-lobby/game-lobby.component';
import { GameSessionComponent } from './Components/Game/game-session/game-session.component';
import { DisplayGameComponent } from './Components/Game/display-game/display-game.component';
import { JoinGameComponent } from './Components/Game/join-game/join-game.component';
import { ChooseProfileComponent } from './Components/Game/choose-profile/choose-profile.component';
import { LeaderboardComponent } from './Components/Game/leaderboard/leaderboard.component';


const routes: Routes = [
  {path:'Home', component:HomePageComponent},
  {path:'Search', component:SearchFlashcardComponent, canActivate: [AuthGuard]},
  {path:'All-quizzes', component:AllQuizzesComponent, canActivate: [AuthGuard]},
  {path:'Create-quiz', component:CreateQuizComponent, canActivate: [AuthGuard]},
  {path:'Create-flashcard', component:CreateSetOfFlashcardsComponent, canActivate: [AuthGuard]},
  {path: 'Display-quiz/:id', component: DisplayQuizComponent, canActivate: [AuthGuard] }, 
  {path: 'Display-flashcard/:id', component: DisplayFlashcardsComponent, canActivate: [AuthGuard] }, 
  {path: 'Display-set-of-flashcard/:id', component: DisplaySetOfFlashcardsComponent, canActivate: [AuthGuard] }, 
  {path: 'GameLobby/:gameCode', component: GameLobbyComponent }, 
  {path: 'GameSession', component: GameSessionComponent, canActivate: [AuthGuard] }, 
  {path:'Join', component: JoinGameComponent},
  {path: 'choose-profile/:gameCode', component: ChooseProfileComponent },
  {path: 'game/:gameCode/play', component: DisplayGameComponent },
  {path: 'leaderboard/:gameCode', component: LeaderboardComponent },



  {path:'Reset-password', component:ResetPasswordComponent},
  {path:'Forgot-password', component:ForgotPasswordComponent},
  {path:'Register', component:RegisterComponent},
  {path:'Login', component:LoginComponent},

  {path:'', redirectTo:'Home', pathMatch:'full'},
  {path:'**',redirectTo:'Home'},
  

  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
