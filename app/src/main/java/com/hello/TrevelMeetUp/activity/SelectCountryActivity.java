package com.hello.TrevelMeetUp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hello.TrevelMeetUp.R;
import com.hello.TrevelMeetUp.common.CommonFunction;
import com.meg7.widget.CircleImageView;

/**
 * Created by lji5317 on 03/01/2018.
 */

public class SelectCountryActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private boolean selectYn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_country);

        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();

        this.db = FirebaseFirestore.getInstance();

        TextView tv = (TextView) findViewById(R.id.user_name);
        tv.setText(this.user.getDisplayName());

        Bitmap profileBitmap = CommonFunction.getBitmapFromURL(this.user.getPhotoUrl().toString());
        CircleImageView imageView = (CircleImageView) findViewById(R.id.user_profile_photo);
        imageView.setImageBitmap(profileBitmap);

        Button button = (Button) findViewById(R.id.saveBtn);

        Spinner spinner = (Spinner) findViewById(R.id.country_list);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_list, R.layout.support_simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectYn = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectYn = false;
            }
        });

        String nation = spinner.getSelectedItem().toString();
        button.setOnClickListener(view -> {

            if(this.selectYn) {
                this.db.collection("member").document(this.user.getUid())
                        .update("nation", nation)
                        .addOnSuccessListener(aVoid -> {
                            startActivity(new Intent(this, SelectRoleActivity.class));
                        });
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
