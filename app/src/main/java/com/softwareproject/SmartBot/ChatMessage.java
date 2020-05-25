package com.softwareproject.SmartBot;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;

public class ChatMessage implements Parcelable {
    private String mMessage;
    private boolean mIsMe ;
    public ChatMessage(String Message, Boolean isMe){
        mMessage = Message;
        mIsMe = isMe;
    }

    protected ChatMessage(Parcel in) {
        mMessage = in.readString();
        mIsMe = in.readByte() != 0;
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    public String getmMessage(){
        return mMessage;
    }

    public Boolean getmIsMe(){
        return mIsMe;
    }


    public ChatMessage(MessageEntity entity) throws ParseException {
        mMessage = entity.text;
        mIsMe = entity.isSend == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mMessage);
        dest.writeByte((byte) (mIsMe ? 1 : 0));
    }
}
