package com.example.apnapay;

public class itemTransaction {
    public String senderUid;
    public String receiverUid;
    public double amount;
    public long timestamp;
    public String type;
    public String senderName;
    public String receiverName;

    public itemTransaction() {}

    public itemTransaction(String senderUid, String receiverUid, double amount, long timestamp, String type, String senderName, String receiverName) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.amount = amount;
        this.timestamp = timestamp;
        this.type = type;
        this.senderName = senderName;
        this.receiverName = receiverName;
    }
}

