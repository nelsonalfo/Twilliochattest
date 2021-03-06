package com.example.miguelsoler.twiliochattest.chat;

import android.content.Context;
import android.util.Log;

import com.example.miguelsoler.twiliochattest.connection.TokenFetcher;
import com.example.miguelsoler.twiliochattest.listeners.TaskCompletionListener;
import com.twilio.accessmanager.AccessManager;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ChatClientListener;


public class ChatClientManager implements AccessManager.Listener, AccessManager.TokenUpdateListener {
    private ChatClient chatClient;

    private AccessManager accessManager;
    private TokenFetcher accessTokenFetcher;
    private ChatClientBuilder chatClientBuilder;


    public ChatClientManager(Context context) {
        this.accessTokenFetcher = new TokenFetcher();
        this.chatClientBuilder = new ChatClientBuilder(context);
    }

    public void setClientListener(ChatClientListener listener) {
        if (this.chatClient != null) {
            this.chatClient.setListener(listener);
        }
    }

    public ChatClient getChatClient() {
        return this.chatClient;
    }

    public void clearChatClient() {
        this.chatClient = null;
    }

    public void connectClient(final TaskCompletionListener<Void, String> listener) {
        ChatClient.setLogLevel(Log.DEBUG);

        accessTokenFetcher.fetch(new TaskCompletionListener<String, String>() {
            @Override
            public void onSuccess(String token) {
                Log.e("ChatClientManager", "connectClient onSuccess " + token);
                createAccessManager(token);
                buildClient(token, listener);
            }

            @Override
            public void onError(String message) {
                if (listener != null) {
                    Log.e("ChatClientManager", "connectClient onError " + message);
                    listener.onError(message);
                }
            }
        });
    }

    private void buildClient(final String token, final TaskCompletionListener<Void, String> listener) {
        chatClientBuilder.build(token, new TaskCompletionListener<ChatClient, String>() {
            @Override
            public void onSuccess(ChatClient chatClient) {
                Log.e("ChatClientManager", "buildClient onSuccess token" + token);

                ChatClientManager.this.chatClient = chatClient;
                listener.onSuccess(null);
            }

            @Override
            public void onError(String message) {
                listener.onError(message);
            }
        });
    }

    private void createAccessManager(String token) {
        Log.e("ChatClientManager", "createAccessManager " + token);
        this.accessManager = new AccessManager(token, this);
        accessManager.addTokenUpdateListener(this);
    }

    @Override
    public void onTokenWillExpire(AccessManager accessManager) {

    }

    @Override
    public void onTokenExpired(AccessManager accessManager) {
        Log.e("ChatClientManager", "token expired.");

        accessTokenFetcher.fetch(new TaskCompletionListener<String, String>() {
            @Override
            public void onSuccess(String token) {
                ChatClientManager.this.accessManager.updateToken(token);
            }

            @Override
            public void onError(String message) {
                Log.e("ChatClientManager", "Error trying to fetch token: " + message);
            }
        });
    }

    @Override
    public void onError(AccessManager accessManager, String s) {
        Log.e("ChatClientManager", "token error: " + s);
    }

    @Override
    public void onTokenUpdated(String s) {
        Log.e("ChatClientManager", "token updated.");
    }

    public void shutdown() {
        if (chatClient != null) {
            chatClient.shutdown();
        }
    }
}
