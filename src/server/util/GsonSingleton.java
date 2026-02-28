package server.util;

import com.google.gson.Gson;

public final class GsonSingleton {

    private static final Gson INSTANCE = new Gson();

    private GsonSingleton() {
    }

    public static Gson getInstance() {
        return INSTANCE;
    }
}