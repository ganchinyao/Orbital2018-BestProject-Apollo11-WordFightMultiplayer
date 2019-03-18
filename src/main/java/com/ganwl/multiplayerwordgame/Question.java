package com.ganwl.multiplayerwordgame;

public class Question {
    private String word;
    private String description;
    private String source;
    private boolean answeredCorrectly;

    public Question(String word, String description, String source) {
        this.word = word;
        this.description = description;
        this.source = source;
    }

    public Question(String word, String desc) {
        this.word = word;
        this.description = desc;
        this.source = "From Wiki";
    }

    public String getWord() {
        return word;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }

    public void setAnsweredCorrectly() {
        this.answeredCorrectly = true;
    }

    public boolean getAnsweredCorrectly() {
        return this.answeredCorrectly;
    }
}
