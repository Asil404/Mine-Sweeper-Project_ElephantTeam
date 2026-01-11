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
                // --- התיקון נמצא בשורה הזו ---
                // הביטוי הזה מפצל לפי פסיקים, אבל מתעלם מפסיקים שנמצאים בתוך מרכאות
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                if (data.length >= 8) {
                    try {
                        // אנחנו מוסיפים גם ניקוי של מרכאות מיותרות (replace) למקרה שהאקסל הוסיף אותן
                        String qText = data[1].trim().replace("\"", ""); 
                        String diffStr = data[2].trim().replace("\"", "");
                        
                        String level;
                        if (diffStr.equals("4") || diffStr.equalsIgnoreCase("Expert")) {
                            level = "Expert";
                        } else if (diffStr.equals("3") || diffStr.equalsIgnoreCase("Hard")) {
                            level = "Hard";
                        } else if (diffStr.equals("2") || diffStr.equalsIgnoreCase("Medium")) {
                            level = "Medium";
                        } else {
                            level = "Easy";
                        }
                        
                        String[] answers = { 
                            data[3].trim().replace("\"", ""), 
                            data[4].trim().replace("\"", ""), 
                            data[5].trim().replace("\"", ""), 
                            data[6].trim().replace("\"", "") 
                        };
                        
                        String correctStr = data[7].trim().toUpperCase().replace("\"", "");
                        int correctIdx = correctStr.equals("D") ? 3 : (correctStr.equals("C") ? 2 : (correctStr.equals("B") ? 1 : 0));
                        
                        questions.add(new Question(qText, answers, correctIdx, level));
                    } catch (Exception ignored) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // הפונקציה שביקשת - מחזירה שאלה רנדומלית מכל המאגר
    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get(new Random().nextInt(questions.size()));
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
                
                // --- תיקון קריטי בשמירה: המרה נכונה של כל 4 הרמות למספרים ---
                String diffNum;
                String lvl = q.getLevel(); // לשם נוחות
                
                if (lvl.equalsIgnoreCase("Expert")) diffNum = "4";
                else if (lvl.equalsIgnoreCase("Hard")) diffNum = "3";
                else if (lvl.equalsIgnoreCase("Medium")) diffNum = "2";
                else diffNum = "1"; // Easy
                
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