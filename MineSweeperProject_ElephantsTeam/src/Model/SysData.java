package Model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SysData {
    private static SysData instance;
    private List<Question> questions;
    private final String CSV_FILE = "questions.csv";

    private SysData() {
        questions = new ArrayList<>();
        loadQuestions();
    }

    public static SysData getInstance() {
        if (instance == null) instance = new SysData();
        return instance;
    }

    public List<Question> getQuestions() { return questions; }

    private void loadQuestions() {
        questions.clear(); 
        File f = new File(CSV_FILE);
        if (!f.exists()) return;

        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            br.readLine(); // דילוג על כותרת
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 8) {
                    try {
                        String qText = data[1].trim(); 
                        String diffNum = data[2].trim();
                        
                        // --- תיקון: זיהוי 4 רמות ---
                        String level;
                        switch (diffNum) {
                            case "1": level = "Easy"; break;
                            case "2": level = "Medium"; break;
                            case "3": level = "Hard"; break;
                            case "4": level = "Expert"; break; // הוספנו את זה!
                            default: level = "Medium";
                        }
                        
                        String[] answers = { data[3].trim(), data[4].trim(), data[5].trim(), data[6].trim() };
                        String correctStr = data[7].trim().toUpperCase();
                        int correctIdx = correctStr.equals("D") ? 3 : (correctStr.equals("C") ? 2 : (correctStr.equals("B") ? 1 : 0));
                        
                        questions.add(new Question(qText, answers, correctIdx, level));
                    } catch (Exception ignored) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- הפונקציה החשובה למשחק: שליפה לפי רמה ---
    public Question getQuestionByLevel(Difficulty gameDiff) {
        if (questions.isEmpty()) return null;
        
        // התאמת הרמה של המשחק למחרוזת בקובץ
        String targetLevel = (gameDiff == Difficulty.HARD) ? "Hard" : ((gameDiff == Difficulty.MEDIUM) ? "Medium" : "Easy");
        
        List<Question> matching = new ArrayList<>();
        for (Question q : questions) {
            if (q.getLevel().equalsIgnoreCase(targetLevel)) {
                matching.add(q);
            }
        }
        
        // גיבוי למקרה שאין שאלות ברמה הזו - מחזיר שאלה רנדומלית כלשהי
        if (matching.isEmpty()) {
            return questions.get(new Random().nextInt(questions.size()));
        }
        
        return matching.get(new Random().nextInt(matching.size()));
    }

    // --- פונקציות לאדמין ---
    public void addQuestion(Question q) {
        questions.add(q);
        saveQuestionsToCSV();
    }

    public void removeQuestion(int index) {
        if (index >= 0 && index < questions.size()) {
            questions.remove(index);
            saveQuestionsToCSV();
        }
    }

    public void saveQuestionsToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(CSV_FILE))) {
            pw.println("ID,Question,Difficulty,A,B,C,D,Correct Answer");
            
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                // המרה חזרה למספרים עבור ה-CSV
                String diffNum = q.getLevel().equalsIgnoreCase("Hard") ? "3" : (q.getLevel().equalsIgnoreCase("Medium") ? "2" : "1");
                
                String correctChar = q.getCorrectAnsIndex() == 3 ? "D" : (q.getCorrectAnsIndex() == 2 ? "C" : (q.getCorrectAnsIndex() == 1 ? "B" : "A"));
                
                String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                        (i + 1), q.getQuestionText(), diffNum,
                        q.getAnswers()[0], q.getAnswers()[1], q.getAnswers()[2], q.getAnswers()[3],
                        correctChar);
                pw.println(line);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}