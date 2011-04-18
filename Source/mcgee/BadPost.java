package mcgee;

public class BadPost {

    private String board;
    private String thread;
    private int wordCount;

    public BadPost(String inBoard, String inThread, int inWordCount) {
        board = inBoard;
        thread = inThread;
        wordCount = inWordCount;
    }

    public String getBoard() {
        return board;
    }

    public String getThread() {
        return thread;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setBoard(String inBoard) {
        board = inBoard;
    }

    public void setThread(String inThread) {
        thread = inThread;
    }

    public void setWordCount(int inWordCount) {
        wordCount = inWordCount;
    }
}
