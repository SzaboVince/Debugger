package com.example.debugging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button buttonAdd;
    private Button buttonModify;
    private Button buttonShowForm;
    private Button buttonBack;
    private EditText editTextId;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextAge;
    private ListView listViewData;
    private LinearLayout linearLayoutForm;
    private ProgressBar progressBar;
    private List<Person> people = new ArrayList<>();
    private String url = "https://retoolapi.dev/T64nIl/people";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        RequestTask task = new RequestTask(url,"GET");
        task.execute();
        buttonShowForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutForm.setVisibility(View.GONE);
                buttonModify.setVisibility(View.VISIBLE);
                buttonShowForm.setVisibility(View.VISIBLE);
                buttonAdd.setVisibility(View.GONE);
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayoutForm.setVisibility(View.VISIBLE);
                buttonShowForm.setVisibility(View.GONE);
            }
        });
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emberHozzaadas();
            }
        });
        buttonModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emberModositas();
            }
        });
    }

    public void init() {
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonModify = findViewById(R.id.buttonModify);
        buttonShowForm = findViewById(R.id.buttonShowForm);
        buttonBack = findViewById(R.id.buttonBack);
        editTextId = findViewById(R.id.editTextId);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAge = findViewById(R.id.editTextAge);
        listViewData = findViewById(R.id.listViewData);
        linearLayoutForm = findViewById(R.id.linearLayoutForm);
        progressBar = findViewById(R.id.progressBar);
        listViewData.setAdapter(new PersonAdapter());
        linearLayoutForm.setVisibility(View.GONE);
    }

    private class PersonAdapter extends ArrayAdapter<Person> {
        public PersonAdapter() {
            super(MainActivity.this, R.layout.list_view_items, people);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.list_view_items, null, false);

            Person actualPerson = people.get(position);
            TextView textViewName = view.findViewById(R.id.textViewName);
            TextView textViewAge = view.findViewById(R.id.textViewAge);
            TextView textViewModify = view.findViewById(R.id.textViewModify);
            TextView textViewDelete = view.findViewById(R.id.textViewDelete);

            textViewName.setText(actualPerson.getName());
            textViewAge.setText(String.valueOf(actualPerson.getAge()));

            textViewModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    linearLayoutForm.setVisibility(View.VISIBLE);
                    editTextId.setText(String.valueOf(actualPerson.getId()));
                    editTextName.setText(actualPerson.getName());
                    editTextEmail.setText(actualPerson.getEmail());
                    editTextAge.setText(String.valueOf(actualPerson.getAge()));
                    buttonModify.setVisibility(View.VISIBLE);
                    buttonBack.setVisibility(View.VISIBLE);
                    buttonAdd.setVisibility(View.GONE);
                    buttonShowForm.setVisibility(View.GONE);
                }
            });

            textViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RequestTask task = new RequestTask(url, "DELETE", String.valueOf(actualPerson.getId()));
                    task.execute();
                }
            });
            return view;
        }
    }

    private void emberModositas() {
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String ageText = editTextAge.getText().toString();
        String idText = editTextId.getText().toString();

        boolean valid = validacio();
        if (valid) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni", Toast.LENGTH_SHORT).show();
        } else {
            int age = Integer.parseInt(ageText);
            int id = Integer.parseInt(idText);
            Person person = new Person(id, name, email, age);
            //implementation("com.google.code.gson:gson:2.10")
            Gson jsonConverter = new Gson();
            RequestTask task = new RequestTask(url + "/" + id, "PUT", jsonConverter.toJson(person));
            task.execute();
        }
    }

    private void emberHozzaadas() {
        String name = editTextName.getText().toString();
        String email = editTextId.getText().toString();
        String ageText = editTextEmail.getText().toString();

        boolean valid = validacio();
        if (valid) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni", Toast.LENGTH_SHORT).show();
        } else {
            int age = Integer.parseInt(ageText);
            Person person = new Person(0, name, email, age);
           //implementation("com.google.code.gson:gson:2.10")
            Gson jsonConverter = new Gson();
            RequestTask task = new RequestTask(url, "POST", jsonConverter.toJson(person));
            task.execute();
        }

    }

    private boolean validacio() {
        if (editTextName.getText().toString().isEmpty() || editTextEmail.getText().toString().isEmpty() || editTextAge.getText().toString().isEmpty())
            return true;
        else return false;
    }

    private void urlapAlaphelyzetbe() {
        editTextAge.setText("");
        editTextEmail.setText("");
        editTextName.setText("");
        linearLayoutForm.setVisibility(View.VISIBLE);
        buttonAdd.setVisibility(View.GONE);
        buttonShowForm.setVisibility(View.GONE);
        buttonModify.setVisibility(View.VISIBLE);
        RequestTask task = new RequestTask(url, "GET");
        task.execute();
    }

    private class RequestTask extends AsyncTask<Void, Void, Response> {
        String requestUrl;
        String requestType;
        String requestParams;

        public RequestTask(String requestUrl, String requestType, String requestParams) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
            this.requestParams = requestParams;
        }

        public RequestTask(String requestUrl, String requestType) {
            this.requestUrl = requestUrl;
            this.requestType = requestType;
        }

        @Override
        protected Response doInBackground(Void... voids) {
            Response response = null;
            try {
                switch (requestType) {
                    case "GET":
                        response = RequestHandler.get(requestUrl);
                        break;
                    case "POST":
                        response = RequestHandler.post(requestUrl, requestParams);
                        break;
                    case "PUT":
                        response = RequestHandler.put(requestUrl, requestParams);
                        break;
                    case "DELETE":
                        response = RequestHandler.delete(requestUrl + "/" + requestParams);
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Response response) {
            super.onPostExecute(response);
            progressBar.setVisibility(View.GONE);
            Gson converter = new Gson();
            if (response.getResponseCode() >= 400) {
                Toast.makeText(MainActivity.this, "Hiba történt a kérés feldolgozása során", Toast.LENGTH_SHORT).show();
                Log.d("onPostExecuteError: ", response.getResponseMessage());
            }
            switch (requestType) {
                case "GET":
                    Person[] peopleArray = converter.fromJson(response.getResponseMessage(), Person[].class);
                    people.clear();
                    people.addAll(Arrays.asList(peopleArray));
                    break;
                case "POST":
                    Person person = converter.fromJson(response.getResponseMessage(), Person.class);
                    people.add(0, person);
                    urlapAlaphelyzetbe();
                    break;
                case "PUT":
                    Person updatePerson = converter.fromJson(response.getResponseMessage(), Person.class);
                    people.replaceAll(person1 -> person1.getId() == updatePerson.getId() ? updatePerson : person1);
                    urlapAlaphelyzetbe();
                    break;
                case "DELETE":
                    int id = Integer.parseInt(requestParams);
                    people.removeIf(person1 -> person1.getId() == id);
                    break;
            }
        }
    }
}