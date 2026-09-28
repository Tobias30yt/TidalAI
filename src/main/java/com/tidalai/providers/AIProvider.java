package com.tidalai.providers;

import java.util.List;

import com.tidalai.models.AIModel;

public interface AIProvider {

    String getName();

    boolean isConnected();

    List<AIModel> getModels();

    String chat(
        String model,
        String prompt
    );
}