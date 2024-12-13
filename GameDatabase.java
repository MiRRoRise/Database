package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class GameDatabase {
    private final Map<Integer, Game> gameMap = new HashMap<>(); // Хеш-таблица для быстрого доступа по id
    private final Map<String, Set<Game>> titleMap = new HashMap<>(); // Хеш-таблица на множестве для быстрого доступа по названию
    private final Map<Double, Set<Game>> ratingMap = new HashMap<>(); // Хеш-таблица на множестве для быстрого доступа по оценке
    private final Map<Date, Set<Game>> releaseDateMap = new HashMap<>(); // Хеш-таблица на множестве для быстрого доступа по дате
    private final String filePath; // Путь к файлу базы данных

    // Конструктор
    public GameDatabase(String filePath) {
        this.filePath = filePath;
        load();
    }

    // Открытие базы данных
    public void load() {
        gameMap.clear();
        titleMap.clear();
        ratingMap.clear();
        releaseDateMap.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Добавляем игры из файла
            while ((line = br.readLine()) != null) {
                Game game = Game.fromString(line);
                addGame(game);
            }
        } catch (IOException e) {
            System.out.println("Создана новая база данных, так как файл не обнаружен");
        }
    }

    // Удаление базы данных
    public void deleteDatabase() {
        clearDatabase();
        File database = new File(filePath);
        if (database.exists() && database.delete()) {
            System.out.println("База данных удалена");
        } else {
            System.err.println("Ошибка удаления базы данных");
        }
    }

    // Очистка базы данных
    public void clearDatabase() {
        gameMap.clear();
        titleMap.clear();
        ratingMap.clear();
    }

    // Сохранение базы данных
    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Game game : gameMap.values()) {
                bw.write(game.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    // Добавление новой игры
    public void addGame(Game game) {
        if (gameMap.containsKey(game.getId())) {
            throw new IllegalArgumentException("Игра с таким ID уже существует.");
        }
        gameMap.put(game.getId(), game);

        if (game.getTitle() != null) {
            titleMap.computeIfAbsent(game.getTitle().toLowerCase(), _ -> new HashSet<>()).add(game);
        }

        if (game.getRating() != null) {
            ratingMap.computeIfAbsent(game.getRating(), _ -> new HashSet<>()).add(game);
        }

        if (game.getReleaseDate() != null) {
            releaseDateMap.computeIfAbsent(game.getReleaseDate(), _ -> new HashSet<>()).add(game);
        }
    }

    // Удаление игры по ключевому полю
    public void removeGame(int id) {
        Game game = gameMap.remove(id);
        if (game != null) {
            String title = game.getTitle();
            if (title != null) {
                titleMap.get(title.toLowerCase()).remove(game);
            }

            Double rating = game.getRating();
            if (rating != null) {
                ratingMap.get(rating).remove(game);
            }

            Date releaseDate = game.getReleaseDate();
            if (releaseDate != null) {
                releaseDateMap.get(releaseDate).remove(game);
            }
        } else {
            System.out.println("Нет игры с ID " + id);
        }
    }

    // Удаление игры по любому полю
    public void removeGameFull(String fieldName, Object value) {
        List<Game> games = searchGame(fieldName, value);
        for (Game game : games) {
            if (fieldName.equalsIgnoreCase("title")) {
                String title = game.getTitle();
                // Проверка на null название
                if (title == null && value == null) {
                    removeGame(game.getId());
                } else if (title != null && title.equalsIgnoreCase((String) value)) {
                    removeGame(game.getId());
                }
            } else {
                removeGame(game.getId());
            }
        }
    }

    // Поиск по значению поля
    public List<Game> searchGame(String fieldName, Object value) {
        List<Game> results = new ArrayList<>();
        // Поиск по id
        if (fieldName.equalsIgnoreCase("id")) {
            if (value instanceof Integer) {
                Game game = gameMap.get(value);
                if (game != null) {
                    results.add(game);
                }
            }
        } else if (fieldName.equalsIgnoreCase("title")) { // Поиск по названию
            if (value == null) {
                for (Game game : gameMap.values()) {
                    if (game.getTitle() == null) {
                        results.add(game);
                    }
                }
            } else if (value instanceof String) {
                Set<Game> games = titleMap.get(value.toString().toLowerCase());
                if (games != null) {
                    results.addAll(games);
                }
            }
        } else if (fieldName.equalsIgnoreCase("rating")) { // Поиск по оценке
            if (value == null) {
                for (Game game : gameMap.values()) {
                    if (game.getRating() == null) {
                        results.add(game);
                    }
                }
            } else if (value instanceof Double) {
                Set<Game> games = ratingMap.get(value);
                if (games != null) {
                    results.addAll(games);
                }
            }
        } else if (fieldName.equalsIgnoreCase("releaseDate")) { // Поиск по дате выхода
            if (value == null) {
                for (Game game : gameMap.values()) {
                    if (game.getReleaseDate() == null) {
                        results.add(game);
                    }
                }
            } else if (value instanceof Date searchDate) {
                Set<Game> games = releaseDateMap.get(searchDate);
                if (games != null) {
                    results.addAll(games);
                }
            }
        }
        return results;
    }

    // Редактирование записи
    public void updateGame(int id, String newTitle, Date newReleaseDate, Double newRating) {
        Game existingGame = gameMap.get(id);
        if (existingGame == null) {
            throw new IllegalArgumentException("Игра с таким ID не найдена");
        }

        // Обновляем название, если оно не пустое
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            String oldTitle = existingGame.getTitle();
            if (oldTitle != null) {
                Set<Game> gamesWithOldTitle = titleMap.get(oldTitle.toLowerCase());
                if (gamesWithOldTitle != null) {
                    gamesWithOldTitle.remove(existingGame);
                }
            }
            existingGame.setTitle(newTitle);
            titleMap.computeIfAbsent(newTitle.toLowerCase(), _ -> new HashSet<>()).add(existingGame);
        }

        // Обновляем дату
        if (newReleaseDate != null) {
            Date oldReleaseDate = existingGame.getReleaseDate();
            if (oldReleaseDate != null) {
                Set<Game> gamesWithOldReleaseDate = releaseDateMap.get(oldReleaseDate);
                if (gamesWithOldReleaseDate != null) {
                    gamesWithOldReleaseDate.remove(existingGame);
                }
            }
            existingGame.setReleaseDate(newReleaseDate);
            releaseDateMap.computeIfAbsent(newReleaseDate, _ -> new HashSet<>()).add(existingGame);
        }

        // Обновляем оценку
        if (newRating != null) {
            Double oldRating = existingGame.getRating();
            if (oldRating != null) {
                Set<Game> gamesWithOldRating = ratingMap.get(oldRating);
                if (gamesWithOldRating != null) {
                    gamesWithOldRating.remove(existingGame);
                }
            }
            existingGame.setRating(newRating);
            ratingMap.computeIfAbsent(newRating, _ -> new HashSet<>()).add(existingGame);
        }
    }

    // Получение всех игр
    public List<Game> getGames() {
        return List.copyOf(gameMap.values());
    }

    // Создание backup-файла
    public void createBackup(String backupFilePath) {
        try (InputStream in = new FileInputStream(filePath);
             OutputStream out = new FileOutputStream(backupFilePath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            System.out.println("Резервная копия успешно создана: " + backupFilePath);
        } catch (IOException e) {
            System.err.println("Ошибка при создании резервной копии: " + e.getMessage());
        }
    }

    // Восстановление из backup-файла
    public void restoreFromBackup(String backupFilePath) {
        try (InputStream in = new FileInputStream(backupFilePath);
             OutputStream out = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            load();
            System.out.println("База данных успешно восстановлена из резервной копии: " + backupFilePath);
        } catch (IOException e) {
            System.err.println("Ошибка при восстановлении из резервной копии: " + e.getMessage());
        }
    }

    // Импорт в файл Excel (.xlsx)
    public void exportToExcel(String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Games");

        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Title");
        headerRow.createCell(2).setCellValue("Release Date");
        headerRow.createCell(3).setCellValue("Rating");

        // Заполняем данными
        int rowNum = 1;
        for (Game game : gameMap.values()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(game.getId());
            row.createCell(1).setCellValue(game.getTitle() != null ? game.getTitle() : "null");
            row.createCell(2).setCellValue(game.getReleaseDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(game.getReleaseDate()) : "null");
            row.createCell(3).setCellValue(game.getRating() != null ? game.getRating() : 0);
        }

        // Записываем в файл
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            System.out.println("Данные успешно экспортированы в " + filePath);
        } catch (IOException e) {
            System.err.println("Ошибка при экспорте данных: " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println("Ошибка при закрытии Workbook: " + e.getMessage());
            }
        }
    }
}
