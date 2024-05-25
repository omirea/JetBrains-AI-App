package org.example.shocintellij;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.shocintellij.commons.JamilaSON;
import org.example.shocintellij.commons.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatGPTController {
    public static final String apiKey = "sk-proj-W8B5lUA5vZgj4QwcKQCsT3BlbkFJ0F5aX0Zuh91G1BT9hari";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    @PostMapping("/api/gpt/translateRo")
    public ResponseEntity<String> getTranslationInRomanian(@org.springframework.web.bind.annotation.RequestBody String code)
    {
        String systemText="You are a highly appreciated and intelligent professor in Computer Science with a Master's in competitive programming that knows very well the Romanian language.";
        String question="Your single job is to translate everything from this code into Romanian. But make sure the code still work. Don't say anything besides the code, just say the code but translated in Romanian. The code is: ";
        String ans=askChatGpt(systemText,question,code);
        if(ans==null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(ans);
    }

    @PostMapping("/api/gpt/isTestable")
        public ResponseEntity<String> getIsItTestable(@org.springframework.web.bind.annotation.RequestBody String code)
    {
        String systemText="You are a highly appreciated and intelligent professor in Computer Science with a Master's in competitive programming.";
        String question="Is this code testable? Please attribute for each line of the following lines number 3 if it is highly testable, number 2 if it is potentially testable or number 1 if it is hardly testable. The" +
                "code is this: ";
        String ans=askChatGpt(systemText,question,code);
        if(ans==null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(ans);


    }
    //@GetMapping("/api/gpt")
    public String askChatGpt(String systemText, String question,String code) {
        OkHttpClient client = new OkHttpClient();
        List<Role> roleList=new ArrayList<>();
        roleList.add(new Role("system",systemText));
        roleList.add(new Role("user",question+code));
        JamilaSON jamilaSON=new JamilaSON("gpt-4o",roleList,2000);
        String code2 = code.replace("\"", "\\\"");
//        String json ="{\n" +
//                "    \"model\": \"gpt-4o\",\n" +
//                "    \"messages\":[\n" +
//                "    {\"role\": \"system\", \"content\": \""+systemText+"\"},\n" +
//                "    {\"role\": \"user\", \"content\": \""+question+code2+"\"\n" +
//             //   "    {\"role\": \"user\", \"content\": \""+question+"String systemText=\"You are a highly appreciated and intelligent professor in Computer Science with a Master's in competitive programming that knows very well the Romanian language.\"\n" +
//                "    }\n" +
//                "  ],\n" +
//                "    \"max_tokens\": 2000\n" +
//                "}";
        ObjectMapper mapper = new ObjectMapper();
        String json="";
        RequestBody body;
        try {
            json = mapper.writeValueAsString(jamilaSON);
            System.out.println("Generated JSON:");
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer sk-proj-W8B5lUA5vZgj4QwcKQCsT3BlbkFJ0F5aX0Zuh91G1BT9hari")
                .post(body)
                .build();


        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;//ResponseEntity.status(response.code()).body("Unexpected code " + response);
            }
            ObjectMapper mapper2 = new ObjectMapper();
            JsonNode rootnode = mapper2.readTree(response.body().string());
            return rootnode.path("choices").get(0).path("message").path("content").asText().toString();
        } catch (IOException e) {
            return null;//ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }
}