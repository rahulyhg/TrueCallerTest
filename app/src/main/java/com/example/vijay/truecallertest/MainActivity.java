package com.example.vijay.truecallertest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueButton;
import com.truecaller.android.sdk.TrueClient;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ITrueCallback, View.OnClickListener{

    private TrueClient mTrueClient;
    private EditText   mProfileName;
    private EditText   mProfilePhone;
    private EditText   mProfileEmail;
    private EditText   mProfileAddress;
    private EditText mProfileJob;
    private TextView mNewValues;

    private String mTruecallerRequestNonce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProfileName = (EditText) findViewById(R.id.name);
        mProfilePhone = (EditText) findViewById(R.id.phone);
        mProfileEmail = (EditText) findViewById(R.id.email);
        mProfileAddress = (EditText) findViewById(R.id.address);
        mProfileJob = (EditText) findViewById(R.id.job);
        mNewValues = (TextView) findViewById(R.id.valuesText);


        TrueButton trueButton = (TrueButton) findViewById(R.id.com_truecaller_android_sdk_truebutton);
        boolean usable = trueButton.isUsable();

        if (usable) {
            mTrueClient = new TrueClient(MainActivity.this, this);
            mTrueClient.setReqNonce("12345678Min");
            trueButton.setTrueClient(mTrueClient);
        } else {
            trueButton.setVisibility(View.GONE);
        }

        findViewById(R.id.customButton).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTruecallerRequestNonce = mTrueClient.generateRequestNonce();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (null != mTrueClient && mTrueClient.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccesProfileShared(@NonNull final TrueProfile trueProfile) {
        final String fullName = trueProfile.firstName + " " + trueProfile.lastName;
        mProfileName.setText(fullName);
        mProfilePhone.setText(trueProfile.phoneNumber);
        mProfileEmail.setText(trueProfile.email);
        mProfileAddress.setText(trueProfile.city);
        final List<String> jobComponents = new ArrayList<>(2);
        if (!TextUtils.isEmpty(trueProfile.jobTitle)) {
            jobComponents.add(trueProfile.jobTitle);
        }
        if (!TextUtils.isEmpty(trueProfile.companyName)) {
            jobComponents.add(trueProfile.companyName);
        }
        mProfileJob.setText(TextUtils.join(" @ ", jobComponents));

        StringBuilder sr = new StringBuilder();
        if (trueProfile.verificationMode != null) {
            sr.append("Verification mode: ").append(trueProfile.verificationMode).append("\n");
        }
        if (trueProfile.verificationTimestamp != 0L) {
            sr.append("Verification Time: ").append(trueProfile.verificationTimestamp).append("\n");
        }

        sr.append("Sim changed: ").append(trueProfile.isSimChanged).append("\n");
        sr.append("RequestNonce: ").append(trueProfile.requestNonce).append("\n");

        mNewValues.setText(sr.toString());

        if (mTruecallerRequestNonce.equals(trueProfile.requestNonce)) {
            Toast.makeText(this, "The request nonce matches", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailureProfileShared(@NonNull final TrueError trueError) {
        Toast.makeText(this, "Failed sharing - Reason: " + trueError.getErrorType(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.customButton:
                mTrueClient.getTruecallerUserProfile(this);
                break;
        }
    }
}
