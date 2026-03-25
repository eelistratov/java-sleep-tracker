package ru.yandex.practicum.sleeptracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SleepTrackerAppTest {

    private List<SleepingSession> testSessions;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные перед каждым тестом
        testSessions = new ArrayList<>();
    }

    // Проверка подсчета общего количества сессий сна
    @Test
    @DisplayName("Анализатор TotalSessionsAnalyzer должен правильно подсчитывать количество сессий")
    void testTotalSessionsAnalyzer() {
        // Подготовка тестовых данных: создаем 3 сессии сна
        LocalDateTime now = LocalDateTime.now();

        SleepingSession session1 = new SleepingSession(
                now.minusDays(3),
                now.minusDays(3).plusHours(8),
                SleepQuality.GOOD
        );

        SleepingSession session2 = new SleepingSession(
                now.minusDays(2),
                now.minusDays(2).plusHours(6),
                SleepQuality.NORMAL
        );

        SleepingSession session3 = new SleepingSession(
                now.minusDays(1),
                now.minusDays(1).plusHours(7),
                SleepQuality.BAD
        );

        testSessions.add(session1);
        testSessions.add(session2);
        testSessions.add(session3);

        // Создаем анализатор и выполняем анализ
        SleepTrackerApp.TotalSessionsAnalyzer analyzer = new SleepTrackerApp.TotalSessionsAnalyzer();
        SleepAnalysisResult result = analyzer.analyze(testSessions);

        // Проверяем результат
        assertEquals("Общее количество сессий сна", result.getDescription());
        assertEquals("3", result.getValue());
        assertNotNull(result.toString());
        assertEquals("Общее количество сессий сна: 3", result.toString());
    }

    // Проверка определения хронотипа пользователя (Сова, Жаворонок, Голубь)
    @Test
    @DisplayName("Анализатор ChronotypeAnalyzer должен правильно определять хронотип пользователя")
    void testChronotypeAnalyzer() {
        // Тестовый сценарий 1: Пользователь с явным хронотипом "Сова"
        // Сова: засыпание после 23:00 и пробуждение после 9:00
        LocalDateTime owlSleepTime = LocalDateTime.of(2024, 1, 15, 23, 30);
        LocalDateTime owlWakeTime = LocalDateTime.of(2024, 1, 16, 10, 0);

        SleepingSession owlSession = new SleepingSession(
                owlSleepTime,
                owlWakeTime,
                SleepQuality.GOOD
        );

        testSessions.add(owlSession);

        // Добавляем еще одну ночную сессию для подтверждения хронотипа
        LocalDateTime owlSleepTime2 = LocalDateTime.of(2024, 1, 16, 23, 45);
        LocalDateTime owlWakeTime2 = LocalDateTime.of(2024, 1, 17, 9, 30);

        SleepingSession owlSession2 = new SleepingSession(
                owlSleepTime2,
                owlWakeTime2,
                SleepQuality.GOOD
        );

        testSessions.add(owlSession2);

        // Создаем анализатор и выполняем анализ
        SleepTrackerApp.ChronotypeAnalyzer analyzer = new SleepTrackerApp.ChronotypeAnalyzer();
        SleepAnalysisResult result = analyzer.analyze(testSessions);

        // Проверяем, что определили "Сову"
        assertEquals("Хронотип пользователя", result.getDescription());
        assertEquals("Сова", result.getValue());

        // Тестовый сценарий 2: Пользователь с явным хронотипом "Жаворонок"
        List<SleepingSession> larkSessions = new ArrayList<>();

        // Жаворонок: засыпание до 22:00 и пробуждение до 7:00
        LocalDateTime larkSleepTime = LocalDateTime.of(2024, 1, 15, 21, 0);
        LocalDateTime larkWakeTime = LocalDateTime.of(2024, 1, 16, 6, 30);

        SleepingSession larkSession = new SleepingSession(
                larkSleepTime,
                larkWakeTime,
                SleepQuality.GOOD
        );

        larkSessions.add(larkSession);

        // Добавляем еще одну сессию жаворонка
        LocalDateTime larkSleepTime2 = LocalDateTime.of(2024, 1, 16, 21, 30);
        LocalDateTime larkWakeTime2 = LocalDateTime.of(2024, 1, 17, 6, 45);

        SleepingSession larkSession2 = new SleepingSession(
                larkSleepTime2,
                larkWakeTime2,
                SleepQuality.GOOD
        );

        larkSessions.add(larkSession2);

        // Выполняем анализ для жаворонка
        SleepAnalysisResult larkResult = analyzer.analyze(larkSessions);

        // Проверяем, что определили "Жаворонка"
        assertEquals("Хронотип пользователя", larkResult.getDescription());
        assertEquals("Жаворонок", larkResult.getValue());

        // Тестовый сценарий 3: Пустой список сессий - должен вернуть "Голубь"
        List<SleepingSession> emptySessions = new ArrayList<>();
        SleepAnalysisResult emptyResult = analyzer.analyze(emptySessions);

        assertEquals("Хронотип пользователя", emptyResult.getDescription());
        assertEquals("Голубь", emptyResult.getValue());
    }

    // Дополнительный тест для демонстрации работы с разными конструкторами SleepAnalysisResult
    @Test
    @DisplayName("SleepAnalysisResult должен корректно работать с разными типами значений")
    void testSleepAnalysisResultConstructors() {
        // Тест с long значением
        SleepAnalysisResult longResult = new SleepAnalysisResult("Тестовый параметр", 123L);
        assertEquals("123", longResult.getValue());
        assertEquals("Тестовый параметр: 123", longResult.toString());

        // Тест с double значением
        SleepAnalysisResult doubleResult = new SleepAnalysisResult("Среднее значение", 45.6789);
        assertEquals("45.68", doubleResult.getValue()); // Проверяем форматирование до 2 знаков
        assertEquals("Среднее значение: 45.68", doubleResult.toString());

        // Тест с String значением
        SleepAnalysisResult stringResult = new SleepAnalysisResult("Статус", "Успешно");
        assertEquals("Успешно", stringResult.getValue());
        assertEquals("Статус: Успешно", stringResult.toString());
    }
}