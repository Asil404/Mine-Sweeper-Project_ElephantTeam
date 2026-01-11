package Model;

public class Question {
    private String questionText;
    private String[] answers;
    private int correctAnsIndex; // 0=A, 1=B, 2=C, 3=D
    private String level;        // Easy, Medium, Hard, Expert

    // בנאי מלא (כולל רמה) - זה מה ש SysData צריך עכשיו
    public Question(String questionText, String[] answers, int correctAnsIndex, String level) {
        this.questionText = questionText;
        this.answers = answers;
        this.correctAnsIndex = correctAnsIndex;
        this.level = level;
    }

    public Question(String questionText, String[] answers, int correctAnsIndex) {
        this(questionText, answers, correctAnsIndex, "Medium"); // ברירת מחדל: בינוני
    }

    // --- Getters & Setters ---

    public String getQuestionText() {
        return questionText;
    }

    public String[] getAnswers() {
        return answers;
    }

    public int getCorrectAnsIndex() {
        return correctAnsIndex;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
    @Override
    public String toString() {
        return questionText; 
    }
}