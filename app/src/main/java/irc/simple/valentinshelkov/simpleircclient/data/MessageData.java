package irc.simple.valentinshelkov.simpleircclient.data;

public class MessageData {
    private final String text;
    private final int textGravity, textColor;
    private long id = -1;

    public MessageData(String text, int textGravity, int textColor) {
        this.text = text;
        this.textGravity = textGravity;
        this.textColor = textColor;
    }

    public String getText() {
        return text;
    }

    public int getTextGravity() {
        return textGravity;
    }

    public int getTextColor() {
        return textColor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
