package com.opencode.android.data.remote.zen;

import com.opencode.android.data.model.zen.ChatRequest;
import com.opencode.android.data.model.zen.ChatResponse;
import com.opencode.android.data.model.zen.ModelResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Retrofit service interface for OpenCode Zen API.
 * API Documentation: https://opencode.ai/docs/zen/
 */
public interface ZenApiService {

    /**
     * Get list of available models from OpenCode Zen.
     * Endpoint: GET https://opencode.ai/zen/v1/models
     */
    @GET("models")
    Call<ModelResponse> getModels(@Header("Authorization") String authToken);

    /**
     * Get a specific model by ID.
     * Endpoint: GET https://opencode.ai/zen/v1/models/{modelId}
     */
    @GET("models/{modelId}")
    Call<ModelResponse.ModelInfo> getModel(
            @Header("Authorization") String authToken,
            @Path("modelId") String modelId);

    /**
     * Create a chat completion using OpenAI-compatible endpoint.
     * Endpoint: POST https://opencode.ai/zen/v1/chat/completions
     * Used for: GLM, Kimi, Qwen, Grok, and other OpenAI-compatible models
     */
    @POST("chat/completions")
    Call<ChatResponse> createCompletion(
            @Header("Authorization") String authToken,
            @Body ChatRequest request);

    /**
     * Create a chat completion (streaming).
     * Endpoint: POST https://opencode.ai/zen/v1/chat/completions
     */
    @POST("chat/completions")
    @Streaming
    Call<ResponseBody> createCompletionStream(
            @Header("Authorization") String authToken,
            @Body ChatRequest request);

    /**
     * Create a response using GPT models.
     * Endpoint: POST https://opencode.ai/zen/v1/responses
     * Used for: GPT models
     */
    @POST("responses")
    Call<ChatResponse> createResponse(
            @Header("Authorization") String authToken,
            @Body ChatRequest request);

    /**
     * Create a message using Claude models.
     * Endpoint: POST https://opencode.ai/zen/v1/messages
     * Used for: Claude models
     */
    @POST("messages")
    Call<ChatResponse> createMessage(
            @Header("Authorization") String authToken,
            @Body ChatRequest request);

    /**
     * Parse streaming response manually.
     */
    @POST("chat/completions")
    @Streaming
    retrofit2.Call<okhttp3.ResponseBody> stream(
            @Header("Authorization") String authToken,
            @Body ChatRequest request);

    /**
     * Custom URL endpoint for flexible API calls.
     */
    @POST
    Call<ChatResponse> post(
            @Header("Authorization") String authToken,
            @Url String url,
            @Body ChatRequest request);

    /**
     * GET request to custom URL.
     */
    @GET
    Call<ModelResponse> get(
            @Header("Authorization") String authToken,
            @Url String url);

    /**
     * Batch get model information.
     */
    @GET("models")
    Call<List<ModelResponse.ModelInfo>> getModelsList(
            @Header("Authorization") String authToken,
            @Query("ids") String modelIds);
}
