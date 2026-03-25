package ru.yandex.practicum.sleeptracker;

import java.time.LocalDateTime;
import java.time.Duration;

// Класс, представляющий одну сессию сна.
public class SleepingSession {

    // Время засыпания (дата и время)
    private final LocalDateTime startDateTime;
    // Время пробуждения (дата и время)
    private final LocalDateTime endDateTime;
    // Качество сна (GOOD, NORMAL, BAD)
    private final SleepQuality quality;

    public SleepingSession(LocalDateTime startDateTime, LocalDateTime endDateTime, SleepQuality quality) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.quality = quality;
    }

    // Возвращает время засыпания
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    // Возвращает время пробуждения
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    // Возвращает качество сна
    public SleepQuality getQuality() {
        return quality;
    }

    // Вычисляет продолжительность сна в минутах
    public long getDurationMinutes() {
        return Duration.between(startDateTime, endDateTime).toMinutes();
    }

    // Возвращает строковое представление сессии сна (для отладки)
    @Override
    public String toString() {
        return "SleepingSession{" +
                "start=" + startDateTime +
                ", end=" + endDateTime +
                ", quality=" + quality +
                '}';
    }
}