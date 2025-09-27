package com.example.retoandroid2;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TheMealDbService {
    @GET("random.php")
    Call<MealResponse> getRandomMeal();
}

