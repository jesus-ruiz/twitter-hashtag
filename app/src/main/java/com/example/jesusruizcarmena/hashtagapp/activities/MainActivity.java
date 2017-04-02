package com.example.jesusruizcarmena.hashtagapp.activities;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.jesusruizcarmena.hashtagapp.R;
import com.example.jesusruizcarmena.hashtagapp.fragments.MainFragment;
import com.example.jesusruizcarmena.hashtagapp.fragments.ResultFragment;
import com.example.jesusruizcarmena.hashtagapp.interfaces.OnSearchListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements OnSearchListener {

    public MainActivity() {
    }

    public MainActivity(Parcel in) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadMainFragment();
    }

    @Override
    public void onSearchInput(String searchText, String token) {
        // Launch Search
        loadResultFragment(searchText, token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Creator<MainActivity> CREATOR = new Parcelable.Creator<MainActivity>() {
        @Override
        public MainActivity createFromParcel(Parcel in) {
            return new MainActivity(in);
        }

        @Override
        public MainActivity[] newArray(int size) {
            return new MainActivity[size];
        }
    };

    private void loadMainFragment() {
        android.support.v4.app.Fragment fragment = MainFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_main, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    private void loadResultFragment(String searchText, String token) {
        android.support.v4.app.Fragment fragment = ResultFragment.newInstance(searchText, token);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layout_main, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }
}
