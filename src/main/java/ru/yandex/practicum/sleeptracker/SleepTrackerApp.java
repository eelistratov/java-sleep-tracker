package ru.yandex.practicum.sleeptracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SleepTrackerApp {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");

    // Список всех анализаторов, которые будут запущены
    private final List<SleepAnalyzer> analyzers;

    // Конструктор. Создаёт и регистрирует все анализаторы.
    public SleepTrackerApp() {
        this.analyzers = List.of(
                new TotalSessionsAnalyzer(),
                new MinDurationAnalyzer(),
                new MaxDurationAnalyzer(),
                new AverageDurationAnalyzer(),
                new BadQualitySessionsAnalyzer(),
                new QualityDistributionAnalyzer(),
                new DaytimeSessionsAnalyzer(),
                new ShortSleepAnalyzer(),
                new SleeplessNightsAnalyzer(),
                new ChronotypeAnalyzer()
        );
    }

    // Запускает все анализаторы на переданных данных
    public List<SleepAnalysisResult> runAnalysis(List<SleepingSession> sessions) {
        return analyzers.stream()
                .map(analyzer -> analyzer.analyze(sessions))
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        // Проверяем, передан ли путь к файлу
        if (args.length < 1) {
            System.err.println("Ошибка: не указан путь к файлу с логом сна");
            System.err.println("Использование: java SleepTrackerApp <путь_к_файлу>");
            System.exit(1);
        }
        String filePath = args[0];
        try {
            // Читаем и парсим файл
            List<SleepingSession> sessions = loadSessionsFromFile(filePath);

            // Проверяем есть ли данные
            if (sessions.isEmpty()) {
                System.out.println("Файл не содержит данных о сессиях сна");
                return;
            }

            // Создаём приложение и запускаем анализ
            SleepTrackerApp app = new SleepTrackerApp();
            List<SleepAnalysisResult> results = app.runAnalysis(sessions);

            // Выводим результаты
            System.out.println(" Результаты анализа сна");
            System.out.println("Всего обработано сессий: " + sessions.size());
            System.out.println();

            results.forEach(System.out::println);

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка формата данных: " + e.getMessage());
            System.exit(1);
        }
    }

    // Читает файл и преобразует каждую строку в объект SleepingSession
    private static List<SleepingSession> loadSessionsFromFile(String filePath) throws IOException {
        return Files.readAllLines(Path.of(filePath)).stream()
                .filter(line -> line != null && !line.isBlank())
                .map(SleepTrackerApp::parseSession)
                .collect(Collectors.toList());
    }

    private static SleepingSession parseSession(String line) {
        String[] parts = line.split(";");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Некорректный формат строки: " + line +
                            ". Ожидается: дата_засыпания;дата_пробуждения;качество"
            );
        }
        try {
            // Парсим дату засыпания
            LocalDateTime start = LocalDateTime.parse(parts[0].trim(), DATE_FORMATTER);
            // Парсим дату пробуждения
            LocalDateTime end = LocalDateTime.parse(parts[1].trim(), DATE_FORMATTER);
            // Парсим качество сна (преобразуем строку в enum)
            SleepQuality quality = SleepQuality.valueOf(parts[2].trim());

            return new SleepingSession(start, end, quality);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Ошибка парсинга даты в строке: " + line, e
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Неизвестное значение качества сна в строке: " + line +
                            ". Допустимые значения: GOOD, NORMAL, BAD", e
            );
        }
    }

    // Анализаторы

    // Подсчёт общего количества сессий сна
    static class TotalSessionsAnalyzer implements SleepAnalyzer {
        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            return new SleepAnalysisResult("Общее количество сессий сна", (long) sessions.size());
        }
    }

    // Минимальная продолжительность сессии сна
    static class MinDurationAnalyzer implements SleepAnalyzer {
        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            long minMinutes = sessions.stream()
                    .mapToLong(SleepingSession::getDurationMinutes)
                    .min()
                    .orElse(0);

            long hours = minMinutes / 60;
            long minutes = minMinutes % 60;
            String formatted = hours + " ч " + minutes + " мин";
            return new SleepAnalysisResult("Минимальная продолжительность сессии сна", formatted);
        }
    }

    // Максимальная продолжительность сессии сна
    static class MaxDurationAnalyzer implements SleepAnalyzer {
        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            long maxMinutes = sessions.stream()
                    .mapToLong(SleepingSession::getDurationMinutes)
                    .max()
                    .orElse(0);

            long hours = maxMinutes / 60;
            long minutes = maxMinutes % 60;
            String formatted = hours + " ч " + minutes + " мин";
            return new SleepAnalysisResult("Максимальная продолжительность сессии сна", formatted);
        }
    }

    // Средняя продолжительность сессии сна
    static class AverageDurationAnalyzer implements SleepAnalyzer {
        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            double avgMinutes = sessions.stream()
                    .mapToLong(SleepingSession::getDurationMinutes)
                    .average()
                    .orElse(0.0);

            long avgHours = (long) avgMinutes / 60;
            long avgMinutesRemainder = (long) avgMinutes % 60;
            String formatted = avgHours + " ч " + avgMinutesRemainder + " мин";
            return new SleepAnalysisResult("Средняя продолжительность сна", formatted);
        }
    }

    // Количество сессий с плохим качеством сна
    static class BadQualitySessionsAnalyzer implements SleepAnalyzer {
        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            long count = sessions.stream()
                    .filter(session -> session.getQuality() == SleepQuality.BAD)
                    .count();

            return new SleepAnalysisResult("Количество сессий с плохим качеством сна (BAD)", count);
        }
    }

    // Распределение сессий по качеству сна
    static class QualityDistributionAnalyzer implements SleepAnalyzer {
        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            long goodCount = sessions.stream()
                    .filter(session -> session.getQuality() == SleepQuality.GOOD)
                    .count();

            long normalCount = sessions.stream()
                    .filter(session -> session.getQuality() == SleepQuality.NORMAL)
                    .count();

            long badCount = sessions.stream()
                    .filter(session -> session.getQuality() == SleepQuality.BAD)
                    .count();

            String result = "GOOD: " + goodCount + ", NORMAL: " + normalCount + ", BAD: " + badCount;
            return new SleepAnalysisResult("Распределение по качеству сна", result);
        }
    }

    // Количество дневных сессий сна (с 6:00 до 21:00)
    static class DaytimeSessionsAnalyzer implements SleepAnalyzer {
        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            long count = sessions.stream()
                    .filter(session -> {
                        int hour = session.getStartDateTime().getHour();
                        return hour >= 6 && hour <= 20;
                    })
                    .count();

            return new SleepAnalysisResult("Количество дневных сессий сна", count);
        }
    }

    // Количество сессий с недостаточным сном (менее 7 часов)
    static class ShortSleepAnalyzer implements SleepAnalyzer {
        private static final long MIN_RECOMMENDED_MINUTES = 7 * 60; // 7 часов в минутах

        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            long count = sessions.stream()
                    .filter(session -> session.getDurationMinutes() < MIN_RECOMMENDED_MINUTES)
                    .count();

            return new SleepAnalysisResult("Количество сессий с недостаточным сном (<7 ч)", count);
        }
    }

    // Количество бессонных ночей
    static class SleeplessNightsAnalyzer implements SleepAnalyzer {
        private static final LocalTime NIGHT_START = LocalTime.of(0, 0);
        private static final LocalTime NIGHT_END = LocalTime.of(6, 0);
        private static final LocalTime NOON = LocalTime.of(12, 0);

        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            if (sessions.isEmpty()) {
                return new SleepAnalysisResult("Количество бессонных ночей", 0);
            }

            SleepingSession firstSession = sessions.stream()
                    .min((s1, s2) -> s1.getStartDateTime().compareTo(s2.getStartDateTime()))
                    .orElseThrow();

            SleepingSession lastSession = sessions.stream()
                    .max((s1, s2) -> s1.getEndDateTime().compareTo(s2.getEndDateTime()))
                    .orElseThrow();

            LocalDate firstNight = determineFirstNight(firstSession);
            LocalDate lastNight = determineLastNight(lastSession);

            Set<LocalDate> nightsWithSleep = sessions.stream()
                    .flatMap(session -> getNightsCoveredBySession(session).stream())
                    .collect(Collectors.toSet());

            long totalNights = java.time.temporal.ChronoUnit.DAYS.between(firstNight, lastNight) + 1;

            long nightsWithSleepInRange = nightsWithSleep.stream()
                    .filter(night -> !night.isBefore(firstNight) && !night.isAfter(lastNight))
                    .count();

            long sleeplessNights = totalNights - nightsWithSleepInRange;

            return new SleepAnalysisResult("Количество бессонных ночей", sleeplessNights);
        }

        private LocalDate determineFirstNight(SleepingSession firstSession) {
            LocalDateTime start = firstSession.getStartDateTime();
            LocalTime startTime = start.toLocalTime();
            LocalDate startDate = start.toLocalDate();

            if (startTime.isAfter(NOON)) {
                return startDate.plusDays(1);
            } else {
                return startDate;
            }
        }

        private LocalDate determineLastNight(SleepingSession lastSession) {
            LocalDateTime end = lastSession.getEndDateTime();
            LocalTime endTime = end.toLocalTime();
            LocalDate endDate = end.toLocalDate();

            if (endTime.isBefore(NOON)) {
                return endDate.minusDays(1);
            } else {
                return endDate;
            }
        }

        private List<LocalDate> getNightsCoveredBySession(SleepingSession session) {
            LocalDateTime start = session.getStartDateTime();
            LocalDateTime end = session.getEndDateTime();

            LocalDate startNight = start.toLocalDate();
            LocalTime startTime = start.toLocalTime();

            if (startTime.isAfter(NIGHT_END)) {
                startNight = startNight.plusDays(1);
            }

            LocalDate endNight = end.toLocalDate();
            LocalTime endTime = end.toLocalTime();

            if (endTime.isBefore(NIGHT_END)) {
                endNight = endNight.minusDays(1);
            }

            if (endNight.isBefore(startNight)) {
                return new ArrayList<>();
            }

            List<LocalDate> nights = new ArrayList<>();
            LocalDate current = startNight;
            while (!current.isAfter(endNight)) {
                nights.add(current);
                current = current.plusDays(1);
            }
            return nights;
        }
    }

    // Анализатор: определение хронотипа пользователя
    static class ChronotypeAnalyzer implements SleepAnalyzer {
        private static final LocalTime OWL_SLEEP_START = LocalTime.of(23, 0);
        private static final LocalTime OWL_WAKE_END = LocalTime.of(9, 0);
        private static final LocalTime LARK_SLEEP_END = LocalTime.of(22, 0);
        private static final LocalTime LARK_WAKE_END = LocalTime.of(7, 0);
        private static final LocalTime NIGHT_START = LocalTime.of(0, 0);
        private static final LocalTime NIGHT_END = LocalTime.of(6, 0);

        enum Chronotype {
            OWL("Сова"),
            LARK("Жаворонок"),
            PIGEON("Голубь");

            private final String displayName;

            Chronotype(String displayName) {
                this.displayName = displayName;
            }

            String getDisplayName() {
                return displayName;
            }
        }

        @Override
        public SleepAnalysisResult analyze(List<SleepingSession> sessions) {
            if (sessions.isEmpty()) {
                return new SleepAnalysisResult("Хронотип пользователя", Chronotype.PIGEON.getDisplayName());
            }

            List<SleepingSession> nightSessions = sessions.stream()
                    .filter(this::isNightSession)
                    .collect(Collectors.toList());

            if (nightSessions.isEmpty()) {
                return new SleepAnalysisResult("Хронотип пользователя", Chronotype.PIGEON.getDisplayName());
            }

            long owlCount = nightSessions.stream()
                    .filter(session -> classifyNight(session) == Chronotype.OWL)
                    .count();

            long larkCount = nightSessions.stream()
                    .filter(session -> classifyNight(session) == Chronotype.LARK)
                    .count();

            long pigeonCount = nightSessions.stream()
                    .filter(session -> classifyNight(session) == Chronotype.PIGEON)
                    .count();

            String resultDisplayName;
            if (owlCount > larkCount && owlCount > pigeonCount) {
                resultDisplayName = Chronotype.OWL.getDisplayName();
            } else if (larkCount > owlCount && larkCount > pigeonCount) {
                resultDisplayName = Chronotype.LARK.getDisplayName();
            } else {
                resultDisplayName = Chronotype.PIGEON.getDisplayName();
            }

            return new SleepAnalysisResult("Хронотип пользователя", resultDisplayName);
        }

        private boolean isNightSession(SleepingSession session) {
            LocalDateTime start = session.getStartDateTime();
            LocalDateTime end = session.getEndDateTime();

            LocalDate startDate = start.toLocalDate();
            LocalDate endDate = end.toLocalDate();

            if (endDate.isAfter(startDate)) {
                return true;
            }

            LocalTime startTime = start.toLocalTime();
            LocalTime endTime = end.toLocalTime();

            return startTime.isBefore(NIGHT_END) || endTime.isAfter(NIGHT_START);
        }

        private Chronotype classifyNight(SleepingSession session) {
            LocalTime sleepTime = session.getStartDateTime().toLocalTime();
            LocalTime wakeTime = session.getEndDateTime().toLocalTime();

            if (sleepTime.isAfter(OWL_SLEEP_START) && wakeTime.isAfter(OWL_WAKE_END)) {
                return Chronotype.OWL;
            }

            if (sleepTime.isBefore(LARK_SLEEP_END) && wakeTime.isBefore(LARK_WAKE_END)) {
                return Chronotype.LARK;
            }

            return Chronotype.PIGEON;
        }
    }
}