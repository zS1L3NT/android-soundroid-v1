package com.zectan.soundroid.Connections;

import com.zectan.soundroid.Classes.Request;

public class DeletePlaylistRequest extends com.zectan.soundroid.Classes.Request {

    public DeletePlaylistRequest(String playlistId, Callback callback) {
        super("http://soundroid.zectan.com/playlist/delete", new Request.Callback() {
            @Override
            public void onComplete(String response) {
                callback.onComplete();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });

        putData("playlistId", playlistId);
        sendRequest(RequestType.DELETE);
    }

    public interface Callback {
        void onComplete();

        void onError(String message);
    }

}