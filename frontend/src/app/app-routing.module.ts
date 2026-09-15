import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomePageComponent } from './Components/Home/home-page/home-page.component';
import { WelcomeComponent } from './Components/Welcome/welcome.component';
import { ProfileComponent } from './Components/Profile/profile.component';
import { AllQuizzesComponent } from './Components/Quizzes/All-quizzes/all-quizzes.component';
import { CreateQuizComponent } from './Components/Quizzes/create-quiz/create-quiz.component';
import { GenerateQuizComponent } from './Components/Quizzes/generate-quiz/generate-quiz.component';
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
import { DisplayGameComponent } from './Components/Game/display-game/display-game.component';
import { JoinGameComponent } from './Components/Game/join-game/join-game.component';
import { ClassesComponent } from './Components/Classes/classes.component';
import { CoursesComponent } from './Components/Courses/courses.component';
import { CreateCourseComponent } from './Components/Courses/create-course.component';
import { CourseViewerComponent } from './Components/Courses/course-viewer.component';
import { QuizHubComponent } from './Components/Quiz-play/quiz-hub.component';
import { TakeQuizComponent } from './Components/Quiz-play/take-quiz.component';
import { GradebookComponent } from './Components/Quiz-play/gradebook.component';
import { PlanningComponent } from './Components/Planning/planning.component';
import { DashboardComponent } from './Components/Dashboard/dashboard.component';
import { TypeTranslationComponent } from './Components/Practice/type-translation.component';


const routes: Routes = [
  {path:'Home', component:HomePageComponent, canActivate: [AuthGuard]},
  {path:'Search', component:SearchFlashcardComponent, canActivate: [AuthGuard]},
  {path:'All-quizzes', component:AllQuizzesComponent, canActivate: [AuthGuard]},
  {path:'Create-quiz', component:CreateQuizComponent, canActivate: [AuthGuard]},
  {path:'Create-flashcard', component:CreateSetOfFlashcardsComponent, canActivate: [AuthGuard]},
  {path: 'Display-quiz/:id', component: DisplayQuizComponent, canActivate: [AuthGuard] }, 
  {path: 'Display-flashcard/:id', component: DisplayFlashcardsComponent, canActivate: [AuthGuard] }, 
  {path: 'Display-set-of-flashcard/:id', component: DisplaySetOfFlashcardsComponent, canActivate: [AuthGuard] }, 
  {path: 'GameLobby/:gameCode', component: GameLobbyComponent }, 
  {path:'Join', component: JoinGameComponent},
  {path: 'game/:gameCode/play', component: DisplayGameComponent },
  {path:'Classes', component: ClassesComponent, canActivate: [AuthGuard]},
  {path:'Courses', component: CoursesComponent, canActivate: [AuthGuard]},
  {path:'Courses/create', component: CreateCourseComponent, canActivate: [AuthGuard]},
  {path:'Courses/:id', component: CourseViewerComponent, canActivate: [AuthGuard]},
  {path:'Quizzes', component: QuizHubComponent, canActivate: [AuthGuard]},
  {path:'Quizzes/generate', component: GenerateQuizComponent, canActivate: [AuthGuard]},
  {path:'Quizzes/:id/take', component: TakeQuizComponent, canActivate: [AuthGuard]},
  {path:'Quizzes/:id/gradebook', component: GradebookComponent, canActivate: [AuthGuard]},
  {path:'Planning', component: PlanningComponent, canActivate: [AuthGuard]},
  {path:'Dashboard', component: DashboardComponent, canActivate: [AuthGuard]},
  {path:'Profile', component: ProfileComponent, canActivate: [AuthGuard]},
  {path:'Practice', component: TypeTranslationComponent, canActivate: [AuthGuard]},



  {path:'Reset-password', component:ResetPasswordComponent},
  {path:'Forgot-password', component:ForgotPasswordComponent},
  {path:'Register', component:RegisterComponent},
  {path:'Login', component:LoginComponent},

  {path:'Welcome', component: WelcomeComponent},
  {path:'', redirectTo:'Welcome', pathMatch:'full'},
  {path:'**', redirectTo:'Welcome'},
  

  
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
