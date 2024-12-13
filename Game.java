package org.example;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Game {
    private final int id; // ID игры
    private String title; // Название игры
    private Date releaseDate; // Дата выхода
    private Double rating; // Оценка игры

    // Конструктор
    public Game(int id, String title, Date releaseDate, Double rating) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public Double getRating() {
        return rating;
    }

    // Сеттеры
    public void setTitle(String title) {
        this.title = title;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    // Создаем игру из строки
    public static Game fromString(String line) {
        String[] parts = line.split(", ");
        int id = Integer.parseInt(parts[0]);
        String title = parts.length > 1 && !parts[1].equals("null") ? parts[1] : null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date releaseDate = null;

        if (parts.length > 2 && !parts[2].equals("null")) {
            try {
                releaseDate = dateFormat.parse(parts[2]);
            } catch (ParseException e) {
                System.err.println("Ошибка парсинга даты: " + e.getMessage());
            }
        }

        Double rating = parts.length > 3 && !parts[3].equals("null") ? Double.parseDouble(parts[3]) : null;
        return new Game(id, title, releaseDate, rating);
    }

    // Возвращаем строковое представление
    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = (releaseDate != null) ? dateFormat.format(releaseDate) : "null";
        return id + ", " + (title != null ? title : "null") + ", " + dateStr + ", " + (rating != null ? rating : "null");
    }
}
