package Model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private static HistoryManager instance;
    private List<GameRecord> records;
    
    // שינינו את הסיומת ל-.dat כי זה קובץ בינארי של ג'אווה ולא טקסט
    private final String FILE_NAME = "game_history.dat"; 

    private HistoryManager() {
        records = new ArrayList<>();
        loadRecords();
    }

    public static HistoryManager getInstance() {
        if (instance == null) {
            instance = new HistoryManager();
        }
        return instance;
    }

    public void addRecord(GameRecord record) {
        records.add(record);
        saveRecords();
    }

    public List<GameRecord> getRecords() {
        return records;
    }

    public void clearAll() {
        records.clear();
        saveRecords();
    }

    // --- שמירה (Serialization) ---
    private void saveRecords() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(records);
        } catch (IOException e) {
            System.err.println("Error saving history: " + e.getMessage());
        }
    }

    // --- טעינה (Deserialization) ---
    @SuppressWarnings("unchecked")
    private void loadRecords() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                records = (List<GameRecord>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading history (creating new): " + e.getMessage());
            records = new ArrayList<>(); // במקרה של שגיאה נתחיל רשימה ריקה
        }
    }
}