
package com.codehelper;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    public String askGemini(String prompt) {
        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "GEMINI_API_KEY environment variable not found"
            );
        }

        try (Client client = Client.builder().apiKey(apiKey).build()) {
            GenerateContentResponse response =
                client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
                );

            return response.text();
        }
    }
}
