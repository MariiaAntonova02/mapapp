package com.example.mapapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NewsReaderActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_news);
    }

    @Override
    public void onListFragmentInteraction(CustomDataGenerator.CustomItem item) {
        Toast.makeText(this, "Item clicked: " + item.id, Toast.LENGTH_LONG).show();
    }
}
