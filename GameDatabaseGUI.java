package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// Класс, представляющий собой графический интерфейс для пользования файловой базой данных
public class GameDatabaseGUI {
    private final GameDatabase gameDatabase;
    private JFrame frame;
    private DefaultTableModel tableModel;
    private JTextField idField, titleField, releaseDateField, ratingField, searchField;
    private JComboBox<String> searchFieldComboBox;

    public GameDatabaseGUI(String filePath) {
        gameDatabase = new GameDatabase(filePath);
        initialize();
    }

    private void initialize() {
        frame = new JFrame("База данных игр");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Создание меню
        createMenu();

        // Создание таблицы
        tableModel = new DefaultTableModel(new String[]{"ID", "Название", "Дата выпуска", "Рейтинг"}, 0);
        JTable gameTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(gameTable);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Панель для ввода данных
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(7, 2));

        inputPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Название:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Дата выпуска (yyyy-MM-dd):"));
        releaseDateField = new JTextField();
        inputPanel.add(releaseDateField);

        inputPanel.add(new JLabel("Рейтинг:"));
        ratingField = new JTextField();
        inputPanel.add(ratingField);

        // Добавление панели поиска
        inputPanel.add(new JLabel("Поиск / Удаление по:"));
        searchFieldComboBox = new JComboBox<>(new String[]{"ID", "Название", "Рейтинг", "Дата выпуска"});
        inputPanel.add(searchFieldComboBox);

        inputPanel.add(new JLabel("Значение поиска:"));
        searchField = new JTextField();
        inputPanel.add(searchField);

        frame.add(inputPanel, BorderLayout.NORTH);

        // Кнопки
        final JPanel buttonPanel = getJPanel();
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Заполнение таблицы данными
        loadGames();

        frame.setVisible(true);
    }

    private JPanel getJPanel() {
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Добавить игру");
        JButton removeButton = new JButton("Удалить игру");
        JButton updateButton = new JButton("Обновить игру");
        JButton searchButton = new JButton("Поиск игры");

        addButton.addActionListener(_ -> addGame());
        removeButton.addActionListener(_ -> removeGame());
        updateButton.addActionListener(_ -> updateGame());
        searchButton.addActionListener(_ -> searchGame());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(searchButton);
        return buttonPanel;
    }

    // Создаем выпадающее меню
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");

        JMenuItem saveItem = new JMenuItem(new AbstractAction("Сохранить базу данных") {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDatabase();
            }
        });

        JMenuItem backupItem = new JMenuItem(new AbstractAction("Создать резервную копию") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createBackup();
            }
        });

        JMenuItem restoreItem = new JMenuItem(new AbstractAction("Восстановить из резервной копии") {
            @Override
            public void actionPerformed(ActionEvent e) {
                restoreBackup();
            }
        });

        JMenuItem exportItem = new JMenuItem(new AbstractAction("Экспорт в Excel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToExcel();
            }
        });

        JMenuItem clearDatabaseItem = new JMenuItem(new AbstractAction("Очистить базу данных") {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearDatabase();
            }
        });

        JMenuItem deleteDatabaseItem = new JMenuItem(new AbstractAction("Удалить базу данных") {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteDatabase();
            }
        });

        fileMenu.add(saveItem);
        fileMenu.add(backupItem);
        fileMenu.add(restoreItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(clearDatabaseItem);
        fileMenu.add(deleteDatabaseItem);

        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
    }

    private void loadGames() {
        List<Game> games = gameDatabase.getGames();
        tableModel.setRowCount(0);
        for (Game game : games) {
            String releaseDateStr = (game.getReleaseDate() != null) ?
                    new SimpleDateFormat("yyyy-MM-dd").format(game.getReleaseDate()) : "null";
            tableModel.addRow(new Object[]{
                    game.getId(),
                    game.getTitle() != null ? game.getTitle() : "null",
                    releaseDateStr,
                    game.getRating() != null ? game.getRating() : "null"
            });
        }
    }

    private void saveDatabase() {
        gameDatabase.save();
        showInfo("База данных успешно сохранена.");
    }

    private void createBackup() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите место для сохранения резервной копии");
        fileChooser.setSelectedFile(new File("backup.db"));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();
            gameDatabase.createBackup(backupFile.getAbsolutePath());
            showInfo("Резервная копия создана: " + backupFile.getAbsolutePath());
        }
    }

    private void restoreBackup() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл резервной копии для восстановления");

        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File backupFile = fileChooser.getSelectedFile();
            gameDatabase.restoreFromBackup(backupFile.getAbsolutePath());
            loadGames();
            showInfo("Резервная копия восстановлена: " + backupFile.getAbsolutePath());
        }
    }

    private void exportToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Сохранить файл Excel");
        fileChooser.setSelectedFile(new File("games.xlsx"));

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            gameDatabase.exportToExcel(fileToSave.getAbsolutePath());
            showInfo("Данные успешно экспортированы в " + fileToSave.getAbsolutePath());
        }
    }

    private void addGame() {
        try {
            if (idField.getText().isEmpty()) {
                showError("Поле ID обязательно для заполнения.");
                return;
            }

            int id = Integer.parseInt(idField.getText());

            String title = titleField.getText().isEmpty() ? null : titleField.getText();
            Date releaseDate = releaseDateField.getText().isEmpty() ? null : new SimpleDateFormat("yyyy-MM-dd").parse(releaseDateField.getText());
            Double rating = ratingField.getText().isEmpty() ? null : Double.parseDouble(ratingField.getText());

            Game newGame = new Game(id, title, releaseDate, rating);
            gameDatabase.addGame(newGame);
            loadGames();
            clearFields();
        } catch (NumberFormatException e) {
            showError("ID должен быть числом.");
        } catch (ParseException e) {
            showError("Неверный формат даты. Используйте yyyy-MM-dd.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void removeGame() {
        executeRemoveOrSearch("remove");
    }

    private void searchGame() {
        executeRemoveOrSearch("search");
    }

    private void executeRemoveOrSearch(String action) {
        String selectedField = (String) searchFieldComboBox.getSelectedItem();
        String value = searchField.getText();
        List<Game> results = null;

        try {
            if (value.isEmpty()) {
                if (action.equals("search")) {
                    loadGames();
                }
                clearFields();
                return;
            }

            switch (selectedField) {
                case "ID":
                    int id = Integer.parseInt(value);
                    if (action.equals("remove")) {
                        gameDatabase.removeGame(id);
                        loadGames();
                        clearFields();
                        return;
                    } else {
                        results = gameDatabase.searchGame("id", id);
                    }
                    break;
                case "Название":
                    if (value.equalsIgnoreCase("null")) {
                        if (action.equals("remove")) {
                            gameDatabase.removeGameFull("title", null);
                            loadGames();
                            clearFields();
                            return;
                        } else {
                            results = gameDatabase.searchGame("title", null);
                        }
                    } else {
                        if (action.equals("remove")) {
                            gameDatabase.removeGameFull("title", value);
                            loadGames();
                            clearFields();
                            return;
                        } else {
                            results = gameDatabase.searchGame("title", value);
                        }
                    }
                    break;
                case "Рейтинг":
                    if (value.equalsIgnoreCase("null")) {
                        if (action.equals("remove")) {
                            gameDatabase.removeGameFull("rating", null);
                            loadGames();
                            clearFields();
                            return;
                        } else {
                            results = gameDatabase.searchGame("rating", null);
                        }
                    } else {
                        double rating = Double.parseDouble(value);
                        if (action.equals("remove")) {
                            gameDatabase.removeGameFull("rating", rating);
                            loadGames();
                            clearFields();
                            return;
                        } else {
                            results = gameDatabase.searchGame("rating", rating);
                        }
                    }
                    break;
                case "Дата выпуска":
                    if (value.equalsIgnoreCase("null")) {
                        if (action.equals("remove")) {
                            gameDatabase.removeGameFull("releaseDate", null);
                            loadGames();
                            clearFields();
                            return;
                        } else {
                            results = gameDatabase.searchGame("releaseDate", null);
                        }
                    } else {
                        Date releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                        if (action.equals("remove")) {
                            gameDatabase.removeGameFull("releaseDate", releaseDate);
                            loadGames();
                            clearFields();
                            return;
                        } else {
                            results = gameDatabase.searchGame("releaseDate", releaseDate);
                        }
                    }
                    break;
                case null:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + selectedField);
            }
        } catch (NumberFormatException e) {
            showError("Неверный ввод: " + e.getMessage());
        } catch (ParseException e) {
            showError("Неверный формат даты: " + e.getMessage());
        }

        if (results != null && !results.isEmpty()) {
            tableModel.setRowCount(0);
            for (Game game : results) {
                String releaseDateStr = (game.getReleaseDate() != null) ?
                        new SimpleDateFormat("yyyy-MM-dd").format(game.getReleaseDate()) : "null";
                tableModel.addRow(new Object[]{
                        game.getId(),
                        game.getTitle() != null ? game.getTitle() : "null",
                        releaseDateStr,
                        game.getRating() != null ? game.getRating() : "null"
                });
            }
        } else {
            showError("Игры не найдены.");
        }
        clearFields();
    }

    private void clearDatabase() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Вы уверены, что хотите очистить базу данных?", "Подтвердите очистку", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            gameDatabase.clearDatabase();
            loadGames();
            clearFields();
        }
    }

    private void deleteDatabase() {
        int confirm = JOptionPane.showConfirmDialog(frame, "Вы уверены, что хотите удалить базу данных?", "Подтвердите удаление", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            gameDatabase.deleteDatabase();
            loadGames();
            clearFields();
        }
    }

    private void updateGame() {
        try {
            int id = Integer.parseInt(idField.getText());
            String title = titleField.getText();
            Date releaseDate = releaseDateField.getText().isEmpty() ? null : new SimpleDateFormat("yyyy-MM-dd").parse(releaseDateField.getText());
            Double rating = ratingField.getText().isEmpty() ? null : Double.parseDouble(ratingField.getText());

            gameDatabase.updateGame(id, title, releaseDate, rating);
            loadGames();
            clearFields();
        } catch (NumberFormatException | ParseException e) {
            showError("Неверный ввод: " + e.getMessage());
        }
    }

    private void clearFields() {
        idField.setText("");
        titleField.setText("");
        releaseDateField.setText("");
        ratingField.setText("");
        searchField.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
}
