package com.hello.Damanna.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hello.Damanna.R;
import com.hello.Damanna.common.CommonFunction;
import com.hello.Damanna.common.RadiusImageView;
import com.hello.Damanna.common.RadiusNetworkImageView;
import com.hello.Damanna.common.VolleySingleton;
import com.hello.Damanna.vo.Photo;
import com.meg7.widget.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lji5317 on 03/01/2018.
 */

public class SelectCountryActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;

    private String nation = "";

    private boolean selectYn = false;

    private static String TAG = "cloudFireStore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_country);

        /*super.actList.add(this);*/

        this.mAuth = FirebaseAuth.getInstance();
        this.user = this.mAuth.getCurrentUser();

        this.db = FirebaseFirestore.getInstance();

        TextView tv = (TextView) findViewById(R.id.user_name);
        tv.setText(this.user.getDisplayName());

        /*Bitmap profileBitmap = CommonFunction.getBitmapFromURL(this.user.getPhotoUrl().toString());*/
        RadiusNetworkImageView imageView = (RadiusNetworkImageView) findViewById(R.id.user_profile_photo);
        imageView.setRadius(175f);
        imageView.setImageUrl(this.user.getPhotoUrl().toString(), VolleySingleton.getInstance(this).getImageLoader());

        /*imageView.setImageBitmap(profileBitmap);*/

        Button button = (Button) findViewById(R.id.saveBtn);

        Spinner spinner = (Spinner) findViewById(R.id.country_list);
        /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.country_list, R.layout.support_simple_spinner_dropdown_item);*/

        List<String> nationList = new ArrayList<>();

        this.db.collection("nation/")
                /*.orderBy("reg_dt", Query.Direction.ASCENDING)*/
                .addSnapshotListener((value, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (DocumentSnapshot document : value) {
                        String nation = document.getString("nation_kr");
                        nationList.add(nation);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, nationList);
                    spinner.setAdapter(adapter);
                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectYn = true;
                nation = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectYn = false;
            }
        });

        button.setOnClickListener(view -> {

            if(this.selectYn) {
                this.db.collection("member").document(this.user.getUid())
                        .update("nation", this.nation)
                        .addOnSuccessListener(aVoid -> {
                            startActivity(new Intent(this, SelectRoleActivity.class));
                            finish();
                        });
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
