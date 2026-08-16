package com.example.subcreator;

public class SubtitleItem {
    private int id;
    private String startTime;
    private String endTime;
    private String originalText;
    private String translatedText;

    public SubtitleItem(int id, String startTime, String endTime, String originalText, String translatedText) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.originalText = originalText;
        this.translatedText = translatedText;
    }

    public int getId() { return id; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public String getTranslatedText() { return translatedText; }
    public void setTranslatedText(String translatedText) { this.translatedText = translatedText; }

    public String toSrtFormat(int mode) {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("\n");
        sb.append(startTime).append(" --> ").append(endTime).append("\n");

        if (mode == 0) {
            sb.append(originalText).append("\n").append(translatedText).append("\n\n");
        } else if (mode == 1) {
            sb.append(translatedText).append("\n\n");
        } else {
            sb.append(originalText).append("\n\n");
        }
        return sb.toString();
    }
}
