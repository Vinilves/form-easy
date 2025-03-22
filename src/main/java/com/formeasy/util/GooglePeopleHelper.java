package com.formeasy.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GooglePeopleHelper {

    /**
     * Obtém as informações do usuário a partir do token de acesso do Google.
     *
     * @param accessToken O token de acesso do Google.
     * @return Um objeto Person contendo as informações do usuário.
     * @throws GeneralSecurityException Se ocorrer um erro de segurança.
     * @throws IOException Se ocorrer um erro de I/O.
     */
    public static Person getUserInfo(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        PeopleService peopleService = new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Form Easy")
                .build();

        return peopleService.people().get("people/me")
                .setPersonFields("names,emailAddresses,photos")
                .execute();
    }
}