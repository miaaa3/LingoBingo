import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomePageComponent } from './Components/Home/home-page/home-page.component';
import { WelcomeComponent } from './Components/Welcome/welcome.component';
import { AvatarComponent } from './Components/Shared/avatar.component';
import { ProfileComponent } from './Components/Profile/profile.component';
import { NavbarComponent } from './Components/Home/navbar/navbar.component';
import { SidebarComponent } from './Components/Home/sidebar/sidebar.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SearchFlashcardComponent } from './Components/Flashcards/search-flashcard/search-flashcard.component';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { AllQuizzesComponent } from './Components/Quizzes/All-quizzes/all-quizzes.component';
import { DisplayQuizComponent } from './Components/Quizzes/Display-quiz/display-quiz.component';
import { CreateQuizComponent } from './Components/Quizzes/create-quiz/create-quiz.component';
import { GenerateQuizComponent } from './Components/Quizzes/generate-quiz/generate-quiz.component';
import { FooterComponent } from './Components/Home/footer/footer.component';
import { LoginComponent } from './Components/Auth/login/login.component';
import { RegisterComponent } from './Components/Auth/register/register.component';
import { ResetPasswordComponent } from './Components/Auth/reset-password/reset-password.component';
import { ForgotPasswordComponent } from './Components/Auth/forgot-password/forgot-password.component';
import { BrowserAnimationsModule, provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr, ToastrModule } from 'ngx-toastr';
import { CreateSetOfFlashcardsComponent } from './Components/Flashcards/Create-set-of-flashcards/create-set-of-flashcards.component';
import { AuthInterceptor } from './Helpers/auth.interceptor';
import { DisplayFlashcardsComponent } from './Components/Flashcards/display-flashcards/display-flashcards.component';
import { CdkDrag, CdkDropList } from '@angular/cdk/drag-drop';
import { DisplaySetOfFlashcardsComponent } from './Components/Flashcards/display-set-of-flashcards/display-set-of-flashcards.component';
import { GameLobbyComponent } from './Components/Game/game-lobby/game-lobby.component';
import { JoinGameComponent } from './Components/Game/join-game/join-game.component';
import { DisplayGameComponent } from './Components/Game/display-game/display-game.component';
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





@NgModule({
  declarations: [
    AppComponent,
    HomePageComponent,
    NavbarComponent,
    SidebarComponent,
    SearchFlashcardComponent,
    AllQuizzesComponent,
    DisplayQuizComponent,
    CreateQuizComponent,
    GenerateQuizComponent,
    TypeTranslationComponent,
    WelcomeComponent,
    AvatarComponent,
    ProfileComponent,
    FooterComponent,
    LoginComponent,
    RegisterComponent,
    ResetPasswordComponent,
    ForgotPasswordComponent,
    CreateSetOfFlashcardsComponent,
    DisplayFlashcardsComponent,
    DisplaySetOfFlashcardsComponent,
    GameLobbyComponent,
    JoinGameComponent,
    DisplayGameComponent,
    ClassesComponent,
    CoursesComponent,
    CreateCourseComponent,
    CourseViewerComponent,
    QuizHubComponent,
    TakeQuizComponent,
    GradebookComponent,
    PlanningComponent,
    DashboardComponent,
    
  
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot(),
    CdkDropList,
    CdkDrag

  ],
  providers: [
    provideAnimations(), // required animations providers
    provideToastr(), 
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
