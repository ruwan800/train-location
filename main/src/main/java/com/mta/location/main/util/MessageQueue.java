package com.mta.location.main.util;

import java.util.ArrayList;
import java.util.List;

public class MessageQueue {

    public static class Message {
        private final int type;
        private final String value;

        public Message(int type, String value) {
            this.type = type;
            this.value = value;
        }

        public int getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    private static MessageQueue instance = new MessageQueue();
    private List<Message> messages = new ArrayList<>();

    private MessageQueue() { }

    public static MessageQueue getInstance() {
        return instance;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void putMessage(int type, String value) {
        messages.add(0, new Message(type, value));
    }
}
