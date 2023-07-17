package com.example.automatic_phone_silencer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class details_page extends AppCompatActivity {
    Button cancel,done;
    EditText startt1,stopt1,ename,edittext1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        cancel=findViewById(R.id.cancel_button);
        done=findViewById(R.id.done_button);
        startt1=findViewById(R.id.start);
        stopt1=findViewById(R.id.stop);
        ename=findViewById(R.id.Eventname);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(details_page.this,MainActivity.class));
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String en=ename.getText().toString();
                String st1=startt1.getText().toString();
                String st2=stopt1.getText().toString();
                if (en.isEmpty() ){
                    ename.setError("This field is required");
                }
                else if (st1.isEmpty() ){
                    startt1.setError("This field is required");
                }
                else if (st2.isEmpty() ){
                    stopt1.setError("This field is required");
                }
                else{

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("eventn", en);
                    resultIntent.putExtra("startt", st1);
                    resultIntent.putExtra("stopt", st2);
                    setResult(RESULT_OK, resultIntent);

                    finish();

                }
            }
        });


        startt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(startt1);
            }
        });

        stopt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(stopt1);
            }
        });
    }

    private void showTimePickerDialog(EditText textView) {
        // Get the current time
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        // Create a new TimePickerDialog instance
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Get the button based on the tag set in showTimePickerDialog()
                TextView selectedTextView = findViewById((Integer) edittext1.getTag());

                // Update the selected textView's text with the selected time
                String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                selectedTextView.setText(selectedTime);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(details_page.this, timeSetListener, hour, minute, false);

        // Set a tag on the textView to identify it later
        edittext1 = textView;
        edittext1.setTag(textView.getId());

        // Show the time picker dialog
        timePickerDialog.show();
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}