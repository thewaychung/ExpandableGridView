package com.zhongzewei.expandablegridviewdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.zhongzewei.widget.ExpandableGridView;

/**
 * This activity is a demo for using ExpandableGridView.
 */
public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initComponent();
    }

    private void initComponent(){

        String[] countryData = {"China", "USA", "UK", "Russia", "France", "Germany", "Japan", "Korea", "Canada"};
        final String[] stateData = {"State A", "State B", "State C", "State D"};

        // 0.Init the ExpandableGridView
        final ExpandableGridView countryGridView = (ExpandableGridView)findViewById(R.id.country_grid);
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getBaseContext(),
                R.layout.grid_item, R.id.grid_item, countryData);
        // 1.Set adapter for the grid view
        countryGridView.setAdapter(countryAdapter);
        // 2.Add click event listener to the grid view, expand grid view when item is clicked
        countryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // expand the grid view
                countryGridView.expandGridViewAtView(view, new ArrayAdapter<String>(getBaseContext(),
                        R.layout.grid_item, R.id.grid_item, stateData));
            }
        });
        // 3.Click event listener of sub GridView items
        countryGridView.setOnExpandItemClickListener(new ExpandableGridView.OnExpandItemClickListener() {
            @Override
            public void onItemClick(int position, Object clickPositionData) {
                Toast.makeText(getBaseContext(), clickPositionData.toString()+" clicked", Toast.LENGTH_LONG).show();
            }
        });
    }

}
