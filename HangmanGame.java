import java.sql.*;
import java.util.*;

public class HangmanGame {
    private static final String[] WORDS = {"java", "programming", "computer", "hangman", "code", "algorithm"};
    private static final int MAX_TRIES = 6;
    private static final String TABLE_NAME = "player_scores";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_POINTS = "points";

    private String playerName;
    private String wordToGuess;
    private char[] guessedLetters;
    private int triesLeft;
    private int playerPoints;
    private Connection con;

    public HangmanGame(String playerName) {
        this.playerName = playerName;
        wordToGuess = WORDS[(int) (Math.random() * WORDS.length)];
        guessedLetters = new char[wordToGuess.length()];
        triesLeft = MAX_TRIES;
        playerPoints = 0;
        try {
            connectToDatabase();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectToDatabase() throws ClassNotFoundException {
        // JDBC URL, username, and password for MySQL database
        final String JDBC_URL = "jdbc:mysql://localhost:3306/hg_dg?characterEncoding=utf8";
        final String USERNAME = "root";
        final String PASSWORD = "";

        // Initialize database connection
        Class.forName("com.mysql.jdbc.Driver");
        try {
            con = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        Scanner scanner = new Scanner(System.in);

        while (triesLeft > 0 && !isWordGuessed()) {
            System.out.println("\nWord: " + getCurrentGuess());
            System.out.println("Tries left: " + triesLeft);
            System.out.print("Enter a letter: ");
            char guess = scanner.next().toLowerCase().charAt(0);

            if (!checkGuess(guess)) {
		printHangman(MAX_TRIES - triesLeft + 1);
                triesLeft--;
            }
        }

        if (isWordGuessed()) {
            System.out.println("\nCongratulations! You guessed the word: " + wordToGuess);
            // Award points based on performance
            playerPoints += triesLeft * 10;
        } else {
            System.out.println("\nSorry, you've run out of tries. The word was: " + wordToGuess);
        }
        savePlayer(playerName, playerPoints); // Save player's name and points to the database
        printLeaderboard(); // Display leaderboard
    }

    private boolean checkGuess(char guess) {
        boolean correctGuess = false;
        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == guess) {
                guessedLetters[i] = guess;
                correctGuess = true;
            }
        }
        if (!correctGuess) {
            System.out.println("Incorrect guess: " + guess);
        }
        return correctGuess;
    }

    private String getCurrentGuess() {
        StringBuilder currentGuess = new StringBuilder();
        for (char c : guessedLetters) {
            currentGuess.append(c == '\0' ? '_' : c);
            currentGuess.append(' ');
        }
        return currentGuess.toString();
    }

    private boolean isWordGuessed() {
        for (char c : guessedLetters) {
            if (c == '\0') {
                return false;
            }
        }
        return true;
    }

public void savePlayer(String name, int points) {
    String selectSQL = "SELECT * FROM player_scores WHERE name = ?";
    String insertSQL = "INSERT INTO player_scores(name, points, games_played) VALUES (?, ?, 1)";
    String updateSQL = "UPDATE player_scores SET points = points + ?, games_played = games_played + 1 WHERE name = ?";
    
    try (PreparedStatement selectStatement = con.prepareStatement(selectSQL);
         PreparedStatement insertStatement = con.prepareStatement(insertSQL);
         PreparedStatement updateStatement = con.prepareStatement(updateSQL)) {
        
        // Check if the player already exists
        selectStatement.setString(1, name);
        ResultSet resultSet = selectStatement.executeQuery();
        
        if (resultSet.next()) {
            // If the player exists, update their points and games played
            updateStatement.setInt(1, points);
            updateStatement.setString(2, name);
            updateStatement.executeUpdate();
        } else {
            // If the player doesn't exist, insert a new row
            insertStatement.setString(1, name);
            insertStatement.setInt(2, points);
            insertStatement.executeUpdate();
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


public void printLeaderboard() {
    String selectSQL = "SELECT name, points, games_played FROM player_scores ORDER BY points DESC";
    try (Statement statement = con.createStatement();
         ResultSet resultSet = statement.executeQuery(selectSQL)) {
        System.out.println("Leaderboard:");
        while (resultSet.next()) {
            String name = resultSet.getString("name");
            int points = resultSet.getInt("points");
            int gamesPlayed = resultSet.getInt("games_played");
            System.out.println(name + ": " + points + " points" + " (Games Played: " + gamesPlayed + ")");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private void printHangman(int attempts) {
        switch (attempts) {
            case 1:
                System.out.println("  ____");
                System.out.println(" |    |");
                System.out.println(" O    |");
                System.out.println("      |");
                System.out.println("      |");
                System.out.println("      |");
                break;
            case 2:
                System.out.println("  ____");
                System.out.println(" |    |");
                System.out.println(" O    |");
                System.out.println(" |    |");
                System.out.println("      |");
                System.out.println("      |");
                break;
            case 3:
                System.out.println("  ____");
                System.out.println(" |    |");
                System.out.println(" O    |");
                System.out.println("/|    |");
                System.out.println("      |");
                System.out.println("      |");
                break;
            case 4:
                System.out.println("  ____");
                System.out.println(" |    |");
                System.out.println(" O    |");
                System.out.println("/|\\   |");
                System.out.println("      |");
                System.out.println("      |");
                break;
            case 5:
                System.out.println("  ____");
                System.out.println(" |    |");
                System.out.println(" O    |");
                System.out.println("/|\\   |");
                System.out.println("/     |");
                System.out.println("      |");
                break;
            case 6:
                System.out.println("  ____");
                System.out.println(" |    |");
                System.out.println(" O    |");
                System.out.println("/|\\   |");
                System.out.println("/ \\   |");
                System.out.println("      |");
                break;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String playerName = scanner.nextLine();
        System.out.println("Welcome to Hangman, " + playerName + "!");
        HangmanGame game = new HangmanGame(playerName);
        game.play();
    }
}
