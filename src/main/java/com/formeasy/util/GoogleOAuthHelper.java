package com.formeasy.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;

public class GoogleOAuthHelper {

    private static final String CLIENT_ID = "714573222235-bavel8mv55lm80o9e18gbdo1rms32kfk.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8888/Callback";

    /**
     * Troca o código de autorização (authCode) por um token de acesso do Google.
     *
     * @param authCode O código de autorização obtido após o login com o Google.
     * @return O token de acesso do Google.
     * @throws IOException Se ocorrer um erro durante a requisição HTTP.
     */
    public static String getAccessToken(String authCode) throws IOException {
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                CLIENT_ID,
                CLIENT_SECRET,
                authCode,
                REDIRECT_URI)
                .execute();

        return tokenResponse.getAccessToken();
    }
}
