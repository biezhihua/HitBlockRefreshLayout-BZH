package com.bzh.hitblockrefreshlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RefreshLayout refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);


        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 150; i++) {
            strings.add("我是别志华" + i);
        }

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings));

    }
}
