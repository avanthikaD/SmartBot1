package com.softwareproject.SmartBot;

public class MessageEntity {
    public String text;
    public int isSend;

    public MessageEntity(String text, int isSend) {
        this.text = text;
        this.isSend = isSend;
    }

    public MessageEntity(ChatMessage message) {
        this.text = message.getmMessage();
        this.isSend = message.getmIsMe() ? 1 : 0;
    }
}
