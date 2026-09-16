package com.lingobingo.Configuration;

import com.lingobingo.Entity.*;
import com.lingobingo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds rich demo data on startup, only when it is missing (idempotent — it
 * bails if the demo teacher already exists). Toggle with APP_SEED_DEMO=false.
 * To reload after changing this file, reset the DB volume: docker compose down -v.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    @Value("${app.seed-demo:true}")
    private boolean seedDemo;

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final LessonRepository lessonRepository;
    private final PlanningEventRepository planningEventRepository;
    private final QuizResultRepository quizResultRepository;
    private final PracticeSessionRepository practiceSessionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!seedDemo) return;
        if (userRepository.findByEmail("teacher@lingobingo.com").isPresent()) return; // already seeded

        // ---------------- Users ----------------
        User teacher = userRepository.save(user("teacher@lingobingo.com", "Sarah Miller", "profteacher", "TEACHER"));
        User maria   = userRepository.save(user("maria@lingobingo.com",   "Maria Lopez",  "maria",       "TEACHER"));

        User alice = userRepository.save(user("alice@lingobingo.com", "Alice Dupont",   "alice", "STUDENT"));
        User bob   = userRepository.save(user("bob@lingobingo.com",   "Bob Martin",     "bob",   "STUDENT"));
        User chloe = userRepository.save(user("chloe@lingobingo.com", "Chloe Bernard",  "chloe", "STUDENT"));
        User omar  = userRepository.save(user("omar@lingobingo.com",  "Omar Haddad",    "omar",  "STUDENT"));
        User lea   = userRepository.save(user("lea@lingobingo.com",   "Lea Rossi",      "lea",   "STUDENT"));
        User sofia = userRepository.save(user("sofia@lingobingo.com", "Sofia Nowak",    "sofia", "STUDENT"));
        User noah  = userRepository.save(user("noah@lingobingo.com",  "Noah Schmidt",   "noah",  "STUDENT"));

        // ---------------- Classes + enrollments ----------------
        Classroom english = classroom("English B2", "Upper-intermediate English — vocabulary and grammar.", teacher, "ENG101");
        enroll(english, alice); enroll(english, bob); enroll(english, chloe); enroll(english, omar); enroll(english, lea);

        Classroom frenchA1 = classroom("French A1", "Beginner French — first words and phrases.", maria, "FR100");
        enroll(frenchA1, sofia); enroll(frenchA1, noah); enroll(frenchA1, alice);

        Classroom spanish = classroom("Spanish Basics", "Starter Spanish vocabulary.", maria, "ESP01");
        enroll(spanish, bob); enroll(spanish, chloe);

        // ---------------- Flashcard sets (translation decks) ----------------
        flashcardSetRepository.save(set("Common Verbs", "Everyday verbs to memorize.", teacher, "en", "fr",
                card("to run", "courir"), card("to eat", "manger"), card("to learn", "apprendre"),
                card("to speak", "parler"), card("to write", "écrire"), card("to read", "lire"),
                card("to sleep", "dormir"), card("to drink", "boire")));

        flashcardSetRepository.save(set("Kitchen & Food", "Food and kitchen vocabulary.", teacher, "en", "fr",
                card("bread", "le pain"), card("cheese", "le fromage"), card("apple", "la pomme"),
                card("water", "l'eau"), card("egg", "l'œuf"), card("milk", "le lait"),
                card("strawberry", "la fraise"), card("chicken", "le poulet")));

        flashcardSetRepository.save(set("Travel Essentials", "Words you need on a trip.", maria, "en", "fr",
                card("airport", "l'aéroport"), card("ticket", "le billet"), card("hotel", "l'hôtel"),
                card("station", "la gare"), card("map", "la carte"), card("luggage", "les bagages")));

        flashcardSetRepository.save(set("Greetings", "Say hello and be polite.", teacher, "en", "fr",
                card("hello", "bonjour"), card("goodbye", "au revoir"), card("please", "s'il vous plaît"),
                card("thank you", "merci"), card("sorry", "désolé"), card("good night", "bonne nuit")));

        flashcardSetRepository.save(set("Numbers 1–10", "Count to ten in Spanish.", maria, "en", "es",
                card("one", "uno"), card("two", "dos"), card("three", "tres"), card("four", "cuatro"),
                card("five", "cinco"), card("six", "seis"), card("seven", "siete"), card("eight", "ocho"),
                card("nine", "nueve"), card("ten", "diez")));

        // ---------------- Quizzes ----------------
        Quiz vocab = quiz("Everyday Vocabulary", "A quick warm-up quiz.", teacher, true,
                question(null, "What is the opposite of 'happy'?", answers("Sad", "Tall", "Fast", "Blue"), "A", 1, "'Sad' is the antonym of 'happy'.", "Think feelings."),
                question(null, "Which word is a fruit?", answers("Carrot", "Apple", "Chair", "River"), "B", 1, "An apple is a fruit.", "You can eat it."),
                question(null, "'Rapide' in English means...", answers("Slow", "Late", "Fast", "Early"), "C", 1, "'Rapide' means fast.", "Speed."),
                question(null, "The plural of 'child' is...", answers("childs", "children", "childes", "child"), "B", 2, "'Children' is irregular.", "Irregular plural."),
                question(null, "Which is a colour?", answers("Purple", "Table", "Quickly", "Under"), "A", 1, "Purple is a colour.", "Rainbow."));

        Quiz grammar = quiz("Present vs Past", "Tense practice.", teacher, true,
                question(null, "She ___ to school every day.", answers("go", "goes", "gone", "going"), "B", 1, "Third person singular adds -s.", "he/she/it."),
                question(null, "Yesterday I ___ a film.", answers("watch", "watches", "watched", "watching"), "C", 2, "Past simple of watch.", "-ed."),
                question(null, "They ___ playing football now.", answers("is", "am", "are", "be"), "C", 1, "Plural subject uses 'are'.", "Present continuous."),
                question(null, "He has ___ his homework.", answers("do", "did", "done", "does"), "C", 2, "Past participle of do.", "have + participle."));

        Quiz frenchQuiz = quiz("French Basics", "Test your first French words.", maria, true,
                question(null, "'Bonjour' means...", answers("Goodbye", "Hello", "Please", "Sorry"), "B", 1, "Bonjour = hello.", "Greeting."),
                question(null, "'Merci' means...", answers("Please", "Yes", "Thank you", "No"), "C", 1, "Merci = thank you.", "Politeness."),
                question(null, "'Le pain' is...", answers("Water", "Bread", "Cheese", "Apple"), "B", 1, "Le pain = bread.", "Bakery."),
                question(null, "'Un chat' is a...", answers("Dog", "Cat", "Bird", "Fish"), "B", 1, "Un chat = a cat.", "Pet."));

        // ---------------- Courses ----------------
        lessonRepository.save(lesson("Introduction to English Tenses", teacher,
                section("Present Simple", "Used for habits and facts. Example: I study every day."),
                section("Present Continuous", "Used for actions happening now. Example: I am studying."),
                section("Past Simple", "Finished actions in the past. Example: I studied yesterday."),
                section("Present Perfect", "Links past to now. Example: I have studied English for 3 years.")));

        lessonRepository.save(lesson("French for Beginners", maria,
                section("Greetings", "Bonjour (hello), Au revoir (goodbye), Merci (thank you)."),
                section("Introducing yourself", "Je m'appelle... (My name is...), J'ai ... ans (I am ... years old)."),
                section("Common nouns", "le pain (bread), l'eau (water), la maison (house).")));

        // ---------------- Quiz results (recent, for dashboard + leaderboard + streaks) ----------------
        // English B2 class — spread across the last few days so streaks show.
        result(vocab, alice, 5, 6, 5, 5, 0); result(grammar, alice, 4, 6, 3, 4, 1); result(vocab, alice, 6, 6, 5, 5, 2);
        result(vocab, bob,   4, 6, 4, 5, 0); result(grammar, bob,   6, 6, 4, 4, 1);
        result(vocab, chloe, 6, 6, 5, 5, 0); result(grammar, chloe, 5, 6, 4, 4, 1); result(vocab, chloe, 5, 6, 4, 5, 3);
        result(vocab, omar,  3, 6, 3, 5, 1); result(grammar, omar,  4, 6, 3, 4, 2);
        result(vocab, lea,   6, 6, 5, 5, 0); result(grammar, lea,   6, 6, 4, 4, 0); result(vocab, lea, 5, 6, 4, 5, 1);
        // French A1 class
        result(frenchQuiz, sofia, 4, 4, 4, 4, 0); result(frenchQuiz, noah, 3, 4, 3, 4, 1); result(frenchQuiz, alice, 4, 4, 4, 4, 0);

        // ---------------- Practice sessions (XP that also feeds streaks) ----------------
        practice(alice, 90, 9, 10, 0); practice(alice, 70, 7, 10, 1); practice(alice, 100, 10, 10, 2);
        practice(bob, 60, 6, 10, 0); practice(bob, 80, 8, 10, 2);
        practice(chloe, 100, 10, 10, 0); practice(lea, 80, 8, 10, 1);

        // ---------------- Planning (upcoming deadlines) ----------------
        planning(english, teacher, "Vocabulary quiz due", "Complete 'Everyday Vocabulary'.", "QUIZ_DUE", 2);
        planning(english, teacher, "Live speaking session", "Join the live game in class.", "LIVE_SESSION", 5);
        planning(english, teacher, "Grammar review", "Present vs Past recap.", "OTHER", 8);
        planning(frenchA1, maria, "French Basics quiz", "First French words quiz.", "QUIZ_DUE", 3);
        planning(spanish, maria, "Numbers practice", "Practice numbers 1–10.", "OTHER", 4);

        System.out.println("[DataSeeder] Rich demo data created. Accounts: teacher@ / maria@ (teachers), " +
                "alice@ / bob@ / chloe@ / omar@ / lea@ / sofia@ / noah@ (students). Password for all: password123.");
    }

    // ---------------- helpers ----------------

    private User user(String email, String name, String username, String role) {
        User u = new User(email, username, passwordEncoder.encode("password123"), role);
        u.setName(name);
        return u;
    }

    private Classroom classroom(String name, String description, User teacher, String joinCode) {
        Classroom c = new Classroom();
        c.setName(name);
        c.setDescription(description);
        c.setTeacher(teacher);
        c.setJoinCode(joinCode);
        return classroomRepository.save(c);
    }

    private void enroll(Classroom classroom, User student) {
        Enrollment e = new Enrollment();
        e.setClassroom(classroom);
        e.setStudent(student);
        enrollmentRepository.save(e);
    }

    private FlashcardSet set(String title, String description, User by, String from, String to, Flashcard... cards) {
        FlashcardSet s = new FlashcardSet();
        s.setTitle(title);
        s.setDescription(description);
        s.setCreatedBy(by);
        s.setIsPublic(true);
        s.setSourceLang(from);
        s.setTargetLang(to);
        List<Flashcard> list = new ArrayList<>();
        for (Flashcard c : cards) { c.setFlashcardSet(s); list.add(c); }
        s.setFlashcards(list);
        return s;
    }

    private Flashcard card(String term, String definition) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition(definition);
        return f;
    }

    private Quiz quiz(String name, String description, User by, boolean isPublic, Question... qs) {
        Quiz q = new Quiz();
        q.setName(name);
        q.setDescription(description);
        q.setCreatedBy(by);
        q.setIsPublic(isPublic);
        List<Question> list = new ArrayList<>();
        for (Question question : qs) { question.setQuiz(q); list.add(question); }
        q.setQuestions(list);
        return quizRepository.save(q);
    }

    private Map<String, String> answers(String a, String b, String c, String d) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("A", a); m.put("B", b); m.put("C", c); m.put("D", d);
        return m;
    }

    private Question question(Quiz quiz, String text, Map<String, String> answers,
                             String correct, int points, String explanation, String tip) {
        Question q = new Question();
        q.setQuestionText(text);
        q.setQuestionDescription("");
        q.setAnswers(answers);
        q.setCorrectAnswer(correct);
        q.setPoints(points);
        q.setExplanation(explanation);
        q.setTip(tip);
        q.setQuiz(quiz);
        return q;
    }

    private Lesson lesson(String title, User by, Section... sections) {
        Lesson l = new Lesson();
        l.setTitle(title);
        l.setCreatedBy(by);
        l.setIsPublic(true);
        l.setIsFavorite(false);
        List<Section> list = new ArrayList<>();
        for (Section s : sections) { s.setLesson(l); list.add(s); }
        l.setSections(list);
        return l;
    }

    private Section section(String heading, String content) {
        Section s = new Section();
        s.setHeading(heading);
        s.setContent(content);
        return s;
    }

    private void result(Quiz quiz, User student, int score, int maxScore, int correct, int total, int daysAgo) {
        QuizResult r = new QuizResult();
        r.setQuiz(quiz);
        r.setStudent(student);
        r.setScore(score);
        r.setMaxScore(maxScore);
        r.setCorrectCount(correct);
        r.setTotalQuestions(total);
        r.setSubmittedAt(LocalDateTime.now().minusDays(daysAgo).withHour(14).withMinute(0));
        quizResultRepository.save(r);
    }

    private void practice(User student, int xp, int correct, int total, int daysAgo) {
        PracticeSession p = new PracticeSession();
        p.setStudent(student);
        p.setXp(xp);
        p.setCorrect(correct);
        p.setTotal(total);
        p.setSubmittedAt(LocalDateTime.now().minusDays(daysAgo).withHour(18).withMinute(0));
        practiceSessionRepository.save(p);
    }

    private void planning(Classroom classroom, User by, String title, String description, String type, int daysFromNow) {
        PlanningEvent e = new PlanningEvent();
        e.setTitle(title);
        e.setDescription(description);
        e.setType(type);
        e.setDueDate(LocalDateTime.now().plusDays(daysFromNow).withHour(12).withMinute(0));
        e.setClassroom(classroom);
        e.setCreatedBy(by);
        planningEventRepository.save(e);
    }
}
