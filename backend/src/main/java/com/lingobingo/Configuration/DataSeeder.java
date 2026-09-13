package com.lingobingo.Configuration;

import com.lingobingo.Entity.*;
import com.lingobingo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds demo data on startup, only when it is missing (idempotent).
 * Toggle with APP_SEED_DEMO=false.
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!seedDemo) return;
        if (userRepository.findByEmail("teacher@lingobingo.com").isPresent()) return; // already seeded

        // --- Users ---
        User teacher = userRepository.save(user("teacher@lingobingo.com", "profteacher", "TEACHER"));
        User alice   = userRepository.save(user("alice@lingobingo.com", "alice", "STUDENT"));
        User bob     = userRepository.save(user("bob@lingobingo.com", "bob", "STUDENT"));

        // --- Class with both students enrolled ---
        Classroom english = new Classroom();
        english.setName("English B2");
        english.setDescription("Upper-intermediate English - vocabulary and grammar.");
        english.setTeacher(teacher);
        english.setJoinCode("ENG101");
        english = classroomRepository.save(english);
        enroll(english, alice);
        enroll(english, bob);

        // --- Public quiz with questions ---
        Quiz quiz = new Quiz();
        quiz.setName("Everyday Vocabulary");
        quiz.setDescription("A quick warm-up quiz.");
        quiz.setCreatedBy(teacher);
        quiz.setIsPublic(true);
        List<Question> questions = new ArrayList<>();
        questions.add(question(quiz, "What is the opposite of 'happy'?",
                answers("Sad", "Tall", "Fast", "Blue"), "A", "'Sad' is the antonym of 'happy'.", "Think feelings."));
        questions.add(question(quiz, "Which word is a fruit?",
                answers("Carrot", "Apple", "Chair", "River"), "B", "An apple is a fruit.", "You can eat it."));
        questions.add(question(quiz, "'Rapide' in English means...",
                answers("Slow", "Late", "Fast", "Early"), "C", "'Rapide' means fast.", "Speed."));
        quiz.setQuestions(questions);
        quizRepository.save(quiz);

        // --- Flashcard set ---
        FlashcardSet set = new FlashcardSet();
        set.setTitle("Common Verbs");
        set.setDescription("Everyday verbs to memorize.");
        set.setCreatedBy(teacher);
        set.setIsPublic(true);
        List<Flashcard> cards = new ArrayList<>();
        cards.add(flashcard(set, "to run", "se courir / courir"));
        cards.add(flashcard(set, "to eat", "manger"));
        cards.add(flashcard(set, "to learn", "apprendre"));
        set.setFlashcards(cards);
        flashcardSetRepository.save(set);

        // --- Course (lesson with sections) ---
        Lesson lesson = new Lesson();
        lesson.setTitle("Introduction to English Tenses");
        lesson.setCreatedBy(teacher);
        lesson.setIsPublic(true);
        lesson.setIsFavorite(false);
        List<Section> sections = new ArrayList<>();
        sections.add(section(lesson, "Present Simple", "Used for habits and facts. Example: I study every day."));
        sections.add(section(lesson, "Present Continuous", "Used for actions happening now. Example: I am studying."));
        lesson.setSections(sections);
        lessonRepository.save(lesson);

        System.out.println("[DataSeeder] Demo data created (teacher@lingobingo.com / student accounts, password: password123).");
    }

    private User user(String email, String username, String role) {
        return new User(email, username, passwordEncoder.encode("password123"), role);
    }

    private void enroll(Classroom classroom, User student) {
        Enrollment e = new Enrollment();
        e.setClassroom(classroom);
        e.setStudent(student);
        enrollmentRepository.save(e);
    }

    private Map<String, String> answers(String a, String b, String c, String d) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("A", a); m.put("B", b); m.put("C", c); m.put("D", d);
        return m;
    }

    private Question question(Quiz quiz, String text, Map<String, String> answers,
                              String correct, String explanation, String tip) {
        Question q = new Question();
        q.setQuestionText(text);
        q.setQuestionDescription("");
        q.setAnswers(answers);
        q.setCorrectAnswer(correct);
        q.setExplanation(explanation);
        q.setTip(tip);
        q.setQuiz(quiz);
        return q;
    }

    private Flashcard flashcard(FlashcardSet set, String term, String definition) {
        Flashcard f = new Flashcard();
        f.setTerm(term);
        f.setDefinition(definition);
        f.setFlashcardSet(set);
        return f;
    }

    private Section section(Lesson lesson, String heading, String content) {
        Section s = new Section();
        s.setHeading(heading);
        s.setContent(content);
        s.setLesson(lesson);
        return s;
    }
}
