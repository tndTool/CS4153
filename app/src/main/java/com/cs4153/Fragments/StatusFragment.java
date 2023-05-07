package com.cs4153.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cs4153.Adapter.MessageAdapter;
import com.cs4153.Message;
import com.cs4153.databinding.FragmentStatusBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StatusFragment extends Fragment {

    FragmentStatusBinding binding;
    List<Message> messageList;

    MessageAdapter messageAdapter;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentStatusBinding.inflate(inflater, container, false);

        messageList = new ArrayList<>();

//        setup recycler view
        messageAdapter = new MessageAdapter(messageList);
        binding.recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(llm);
        llm.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(llm);


        binding.sendBtn.setOnClickListener((v) -> {
            String question = binding.messageEditText.getText().toString().trim();

            addToChat(question, Message.SENT_BY_ME);
            binding.messageEditText.setText("");
            callAPI(question);
            binding.welcomeText.setVisibility(View.GONE);
        });

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    void addToChat(String message, String sentBy) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                binding.recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    void addResponse(String response) {
        messageList.remove(messageList.size()-1);
        addToChat(response, Message.SENT_BY_BOT);
    }

    void callAPI(String question) {
//        okhttp

        messageList.add(new Message("Typing... ", Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "text-davinci-003");
            jsonBody.put("prompt", question);
            jsonBody.put("max_tokens", 4000);
            jsonBody.put("temperature", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body().toString());
                }
            }
        });
    }
}