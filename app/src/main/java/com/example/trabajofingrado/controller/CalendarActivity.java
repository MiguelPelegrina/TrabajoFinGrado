package com.example.trabajofingrado.controller;


import static com.example.trabajofingrado.utilities.Utils.CALENDAR_REFERENCE;
import static com.example.trabajofingrado.utilities.Utils.RECIPE_REFERENCE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.trabajofingrado.R;
import com.example.trabajofingrado.adapter.RecipeRecyclerAdapter;
import com.example.trabajofingrado.model.Recipe;
import com.example.trabajofingrado.model.RecipesDay;
import com.example.trabajofingrado.utilities.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CalendarActivity extends BaseActivity {
    // Fields
    // Of class
    private static final int RECIPE_CHOICE_RESULT_CODE = 1;

    // Of instance
    private int position;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private Button btnAddRecipe;
    private CalendarView calendarView;
    private List<EventDay> events = new ArrayList<>();

    private RecipesDay selectedRecipesDay;
    private RecipeRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private RecyclerView.ViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Bind the views
        bindViews();

        // Configure the drawer layout
        setDrawerLayout(R.id.nav_calendar);

        // Configure the recyclerView and their adapter
        setRecyclerView();

        // Configure the listener
        setListener();


        setCookingDays();
    }

    private void setCookingDays() {
        Query query = CALENDAR_REFERENCE.orderByChild("date");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RecipesDay recipesDay = ds.getValue(RecipesDay.class);
                    // TODO
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(recipesDay.getDate());
                    events.add(new EventDay(calendar));
                }
                calendarView.setEvents(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
        /*events.add(new RecipesDay(new GregorianCalendar(2023, 5, 25)));
        events.add(new RecipesDay(new GregorianCalendar(2023, 5, 26)));
        calendarView.setEvents(events);*/
    }

    /**
     * Binds the views of the activity and the layout
     */
    private void bindViews() {
        btnAddRecipe = findViewById(R.id.btnAddRecipeCalendar);
        calendarView = findViewById(R.id.calendarView);
        drawerLayout = findViewById(R.id.drawer_layout_calendar);
        toolbar = findViewById(R.id.toolbar_calendar);
        recyclerView = findViewById(R.id.rvCalendarRecipes);
    }

    /**
     * Configures the recycler view
     */
    private void setRecyclerView() {
        // Instance the adapter
        this.recyclerAdapter = new RecipeRecyclerAdapter(recipeList);

        // Instance the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Configure the recycler view
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        // Set the data

    }

    private void setListener() {
        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                selectedRecipesDay = new RecipesDay(clickedDayCalendar.getTimeInMillis());
                fillRecipesList(selectedRecipesDay.getDate());

                // events.add(new RecipesDay(clickedDayCalendar));
                // calendarView.setEvents(events);
            }
        });

        btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CalendarActivity.this, RecipeListActivity.class);
                intent.putExtra("recipesDayDate", selectedRecipesDay.getDate());
                startActivity(intent);
            }
        });

        recyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        recyclerView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return false;
            }
        });
    }

    /**
     * Fills the recipe list with all the recipes from the database
     */
    private void fillRecipesList(Long date) {
        // Set the database to get all the recipes
        Query queryCalendar = CALENDAR_REFERENCE.orderByKey().equalTo(String.valueOf(date));
        queryCalendar.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the actual list
                recipeList.clear();
                recyclerAdapter.notifyDataSetChanged();

                // Get the recipes of the selected day
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RecipesDay recipesDay = ds.getValue(RecipesDay.class);
                    for (String recipe : recipesDay.getRecipes()) {
                        Query queryRecipes = RECIPE_REFERENCE.orderByChild("id").equalTo(recipe);
                        queryRecipes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                Toasty.info(CalendarActivity.this, selectedRecipesDay.getDate() + "").show();
                                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                                    recipeList.add(recipe);
                                }
                                recyclerAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Utils.connectionError(CalendarActivity.this);
                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utils.connectionError(CalendarActivity.this);
            }
        });
    }
}