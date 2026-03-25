package ru.yandex.practicum.sleeptracker;
import java.util.List;

// Интерфейс для всех анализаторов сна.
@FunctionalInterface
public interface SleepAnalyzer {
    SleepAnalysisResult analyze(List<SleepingSession> sessions);
}