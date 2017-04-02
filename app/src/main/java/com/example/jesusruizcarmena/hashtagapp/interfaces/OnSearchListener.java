package com.example.jesusruizcarmena.hashtagapp.interfaces;

import android.os.Parcelable;

/**
 * Created by jesusruizcarmena on 3/26/17.
 */

public interface OnSearchListener extends Parcelable {
    void onSearchInput(String query, String token);
}
