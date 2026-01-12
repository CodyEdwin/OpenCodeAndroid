package com.opencode.android.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.opencode.android.data.remote.zen.ZenApiService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Network utility class for providing Retrofit and OkHttp instances.
 * This class provides static methods for network configuration.
 */
public final class NetworkModule {

    private static final String PREF_NAME = "secure_prefs";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_API_BASE_URL = "api_base_url";

    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 60;
    private static final int WRITE_TIMEOUT = 30;

    private NetworkModule() {
        // Utility class - prevent instantiation
    }

    /**
     * Create an OkHttpClient with logging and auth interceptors.
     */
    public static OkHttpClient provideOkHttpClient(Context context) {
        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor for adding API key
        AuthInterceptor authInterceptor = new AuthInterceptor(context);

        // Build OkHttpClient
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * Create a Retrofit instance.
     */
    public static Retrofit provideRetrofit(Context context, OkHttpClient okHttpClient) {
        // Get base URL from preferences or use default
        String baseUrl = getBaseUrl(context);

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    /**
     * Create a ZenApiService instance.
     */
    public static ZenApiService provideZenApiService(Context context) {
        OkHttpClient okHttpClient = provideOkHttpClient(context);
        Retrofit retrofit = provideRetrofit(context, okHttpClient);
        return retrofit.create(ZenApiService.class);
    }

    /**
     * Get base URL from preferences.
     */
    public static String getBaseUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("opencode_prefs", Context.MODE_PRIVATE);
        return prefs.getString(KEY_API_BASE_URL, "https://opencode.ai/zen/v1/");
    }

    /**
     * Get API key from secure preferences.
     */
    public static String getApiKey(Context context) {
        try {
            SharedPreferences prefs = getSecurePreferences(context);
            return prefs.getString(KEY_API_KEY, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Save API key to secure preferences.
     */
    public static void saveApiKey(Context context, String apiKey) {
        try {
            SharedPreferences prefs = getSecurePreferences(context);
            prefs.edit().putString(KEY_API_KEY, apiKey).apply();
        } catch (Exception e) {
            Log.e("NetworkModule", "Error saving API key", e);
        }
    }

    private static SharedPreferences getSecurePreferences(Context context) throws Exception {
        MasterKey masterKey = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();

        return EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    private static class Log {
        public static void e(String tag, String message, Exception e) {
            android.util.Log.e(tag, message, e);
        }
    }

    /**
     * Interceptor for adding authentication headers.
     */
    private static class AuthInterceptor implements Interceptor {

        private static final String AUTH_HEADER = "Authorization";
        private static final String BEARER_PREFIX = "Bearer ";
        private final Context context;

        AuthInterceptor(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request originalRequest = chain.request();

            // Get API key from secure storage
            String apiKey = getApiKey(context);

            if (apiKey == null || apiKey.isEmpty()) {
                // No API key, proceed without auth
                return chain.proceed(originalRequest);
            }

            // Add authorization header
            Request authenticatedRequest = originalRequest.newBuilder()
                    .header(AUTH_HEADER, BEARER_PREFIX + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            return chain.proceed(authenticatedRequest);
        }

        private String getApiKey(Context context) {
            try {
                SharedPreferences prefs = getSecurePreferences(context);
                return prefs.getString(KEY_API_KEY, null);
            } catch (Exception e) {
                return null;
            }
        }

        private SharedPreferences getSecurePreferences(Context context) throws Exception {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        }
    }
}
