package ru.yandex.practicum.sleeptracker;

// Класс-обёртка для результата работы анализатора.
public class SleepAnalysisResult {

    // Описание результата
    private final String description;
    private final String value;

    // Конструктор для результата с текстовым описанием и числовым значением
    public SleepAnalysisResult(String description, long value) {
        this.description = description;
        this.value = String.valueOf(value);
    }

    // Конструктор для результата с текстовым описанием и вещественным значением
    public SleepAnalysisResult(String description, double value) {
        this.description = description;
        this.value = String.format("%.2f", value);
    }

    // Конструктор для результата с текстовым описанием и строковым значением
    public SleepAnalysisResult(String description, String value) {
        this.description = description;
        this.value = value;
    }

    // Возвращает описание результата
    public String getDescription() {
        return description;
    }

    // Возвращает значение результата в виде строки
    public String getValue() {
        return value;
    }

    // Преобразует результат в удобный для вывода формат
    @Override
    public String toString() {
        return description + ": " + value;
    }
}