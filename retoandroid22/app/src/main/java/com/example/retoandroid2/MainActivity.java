package com.example.retoandroid2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TheMealDbService mealDbService;
    private Meal currentMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnGetRecipe = findViewById(R.id.btn_get_recipe);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        CardView cardRecipe = findViewById(R.id.card_recipe);
        ImageView imgRecipeThumb = findViewById(R.id.img_recipe_thumb);
        TextView txtRecipeTitle = findViewById(R.id.txt_recipe_title);
        TextView txtRecipeCategory = findViewById(R.id.txt_recipe_category);
        TextView txtRecipeArea = findViewById(R.id.txt_recipe_area);
        TextView txtRecipeIngredients = findViewById(R.id.txt_recipe_ingredients);
        TextView txtRecipeInstructions = findViewById(R.id.txt_recipe_instructions);
        Button btnFavorite = findViewById(R.id.btn_favorite);
        Button btnAnother = findViewById(R.id.btn_another);
        TextView txtYoutube = findViewById(R.id.txt_youtube);
        TextView txtSource = findViewById(R.id.txt_source);

        // Inicialmente ocultar la tarjeta de receta
        cardRecipe.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        // Inicializar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mealDbService = retrofit.create(TheMealDbService.class);

        btnGetRecipe.setOnClickListener(v -> {
            getRandomMeal(progressBar, cardRecipe, imgRecipeThumb, txtRecipeTitle, txtRecipeCategory, txtRecipeArea, txtRecipeIngredients, txtRecipeInstructions, txtYoutube, txtSource);
        });

        btnAnother.setOnClickListener(v -> {
            getRandomMeal(progressBar, cardRecipe, imgRecipeThumb, txtRecipeTitle, txtRecipeCategory, txtRecipeArea, txtRecipeIngredients, txtRecipeInstructions, txtYoutube, txtSource);
        });

        btnFavorite.setOnClickListener(v -> {
            if (currentMeal != null) {
                Toast.makeText(this, "Receta guardada en favoritos (demo)", Toast.LENGTH_SHORT).show();
                // Aquí puedes implementar la lógica real de favoritos
            }
        });
    }

    // Función para traducir texto usando LibreTranslate
    private void traducirInstrucciones(String textoOriginal, OnTranslationReadyListener listener) {
        if (textoOriginal == null || textoOriginal.trim().isEmpty()) {
            listener.onTranslationReady("");
            return;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("q", textoOriginal)
                .add("source", "en")
                .add("target", "es")
                .add("format", "text")
                .build();
        Request request = new Request.Builder()
                .url("https://libretranslate.de/translate")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onTranslationReady(textoOriginal); // Si falla, mostrar original
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String translated = json.getString("translatedText");
                        listener.onTranslationReady(translated);
                    } catch (Exception e) {
                        listener.onTranslationReady(textoOriginal);
                    }
                } else {
                    listener.onTranslationReady(textoOriginal);
                }
            }
        });
    }
    // Interfaz para callback de traducción (funcional)
    interface OnTranslationReadyListener {
        void onTranslationReady(String textoTraducido);
    }
    // Traduce varios textos separados por '|||' y retorna la lista traducida
    private void traducirLista(String textosConcatenados, OnTranslationListReadyListener listener) {
        if (textosConcatenados == null || textosConcatenados.trim().isEmpty()) {
            listener.onTranslationListReady(new String[]{});
            return;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("q", textosConcatenados)
                .add("source", "en")
                .add("target", "es")
                .add("format", "text")
                .build();
        Request request = new Request.Builder()
                .url("https://libretranslate.de/translate")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.onTranslationListReady(textosConcatenados.split("\\|\\|\\|"));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String translated = json.getString("translatedText");
                        String[] parts = translated.split("\\|\\|\\|");
                        listener.onTranslationListReady(parts);
                    } catch (Exception e) {
                        listener.onTranslationListReady(textosConcatenados.split("\\|\\|\\|"));
                    }
                } else {
                    listener.onTranslationListReady(textosConcatenados.split("\\|\\|\\|"));
                }
            }
        });
    }
    interface OnTranslationListReadyListener {
        void onTranslationListReady(String[] textosTraducidos);
    }

    private void getRandomMeal(ProgressBar progressBar, CardView cardRecipe, ImageView imgRecipeThumb, TextView txtRecipeTitle, TextView txtRecipeCategory, TextView txtRecipeArea, TextView txtRecipeIngredients, TextView txtRecipeInstructions, TextView txtYoutube, TextView txtSource) {
        progressBar.setVisibility(View.VISIBLE);
        cardRecipe.setVisibility(View.GONE);
        mealDbService.getRandomMeal().enqueue(new retrofit2.Callback<MealResponse>() {
            @Override
            public void onResponse(retrofit2.Call<MealResponse> call, retrofit2.Response<MealResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().meals != null && !response.body().meals.isEmpty()) {
                    Meal meal = response.body().meals.get(0);
                    currentMeal = meal;
                    // Traducir título, categoría y área juntos
                    String titCatArea = meal.strMeal + "|||" + meal.strCategory + "|||" + meal.strArea;
                    traducirLista(titCatArea, new OnTranslationListReadyListener() {
                        @Override
                        public void onTranslationListReady(String[] textosTraducidos) {
                            runOnUiThread(() -> {
                                if (textosTraducidos.length >= 3) {
                                    txtRecipeTitle.setText(textosTraducidos[0]);
                                    txtRecipeCategory.setText("Categoría: " + textosTraducidos[1]);
                                    txtRecipeArea.setText("Origen: " + textosTraducidos[2]);
                                }
                            });
                        }
                    });
                    // Traducción de instrucciones (validación de null)
                    traducirInstrucciones(meal.strInstructions, new OnTranslationReadyListener() {
                        @Override
                        public void onTranslationReady(String textoTraducido) {
                            runOnUiThread(() -> txtRecipeInstructions.setText("Instrucciones: " + textoTraducido));
                        }
                    });
                    // Traducir ingredientes y medidas juntos
                    StringBuilder ingredientesConcat = new StringBuilder();
                    StringBuilder medidasConcat = new StringBuilder();
                    int count = 0;
                    for (int i = 1; i <= 20; i++) {
                        try {
                            java.lang.reflect.Field ingField = meal.getClass().getField("strIngredient" + i);
                            java.lang.reflect.Field meaField = meal.getClass().getField("strMeasure" + i);
                            String ingredient = (String) ingField.get(meal);
                            String measure = (String) meaField.get(meal);
                            if (ingredient != null && !ingredient.trim().isEmpty()) {
                                if (count > 0) {
                                    ingredientesConcat.append("|||");
                                    medidasConcat.append("|||");
                                }
                                ingredientesConcat.append(ingredient);
                                medidasConcat.append(measure != null ? measure : "");
                                count++;
                            }
                        } catch (Exception e) {
                            // Ignorar campos no existentes
                        }
                    }
                    traducirLista(ingredientesConcat.toString(), new OnTranslationListReadyListener() {
                        @Override
                        public void onTranslationListReady(String[] ingredientesTraducidos) {
                            traducirLista(medidasConcat.toString(), new OnTranslationListReadyListener() {
                                @Override
                                public void onTranslationListReady(String[] medidasTraducidas) {
                                    runOnUiThread(() -> {
                                        StringBuilder ingredientsBuilder = new StringBuilder();
                                        for (int j = 0; j < ingredientesTraducidos.length; j++) {
                                            ingredientsBuilder.append("- ").append(ingredientesTraducidos[j]).append(" (")
                                                    .append(j < medidasTraducidas.length ? medidasTraducidas[j] : "")
                                                    .append(")\n");
                                        }
                                        txtRecipeIngredients.setText("Ingredientes:\n" + ingredientsBuilder.toString());
                                    });
                                }
                            });
                        }
                    });
                    // Cargar imagen con Glide
                    Glide.with(imgRecipeThumb.getContext())
                            .load(meal.strMealThumb)
                            .centerCrop()
                            .into(imgRecipeThumb);
                    // Enlaces
                    if (meal.strYoutube != null && !meal.strYoutube.isEmpty()) {
                        txtYoutube.setVisibility(View.VISIBLE);
                        txtYoutube.setOnClickListener(v -> {
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(meal.strYoutube));
                            v.getContext().startActivity(intent);
                        });
                    } else {
                        txtYoutube.setVisibility(View.GONE);
                    }
                    if (meal.strSource != null && !meal.strSource.isEmpty()) {
                        txtSource.setVisibility(View.VISIBLE);
                        txtSource.setOnClickListener(v -> {
                            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(meal.strSource));
                            v.getContext().startActivity(intent);
                        });
                    } else {
                        txtSource.setVisibility(View.GONE);
                    }
                    cardRecipe.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, "No se pudo obtener la receta", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(retrofit2.Call<MealResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}