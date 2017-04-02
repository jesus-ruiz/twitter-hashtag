package com.example.jesusruizcarmena.hashtagapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jesusruizcarmena.hashtagapp.R;
import com.example.jesusruizcarmena.hashtagapp.interfaces.OnSearchListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.network.HttpRequest;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // The fragment initialization parameters
    private static final String ARG_SEARCH_LISTENER = "arg_search_listener";
    private OnSearchListener onSearchListener;
    private String token;
    private LinkedHashSet<String> history;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param onSearchListener Listens to search events
     * @return A new instance of fragment MainFragment.
     */
    public static MainFragment newInstance(OnSearchListener onSearchListener) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SEARCH_LISTENER, onSearchListener);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            onSearchListener = getArguments().getParcelable(ARG_SEARCH_LISTENER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);
        history = new LinkedHashSet<>();
        final EditText editText = ButterKnife.findById(view, R.id.edit_text_search_input);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                final String searchText = textView.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !searchText.equals("")) {
                    // Save in History
                    writeHistory(searchText);
                    // Launch the search
                    onSearchListener.onSearchInput(searchText, token);
                    hideKeyboard();
                }
                return false;
            }
        });
        initTwitter();
        return view;
    }

    private void readHistory() {
        try {
            FileInputStream fis = getActivity().openFileInput("history");
            byte[] byteData = new byte[1024];
            try {
                int result = fis.read(byteData);
                LinearLayout layoutHistory = ButterKnife.findById(getActivity(), R.id.layout_history);
                while (result > 0) {
                    final String item = new String(byteData, Charset.defaultCharset());
                    if (history.add(item)) {
                        TextView historyItem = new TextView(getContext());
                        historyItem.setText("#" + item.trim());
                        historyItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onSearchListener.onSearchInput(item, token);
                                hideKeyboard();
                            }
                        });
                        layoutHistory.addView(historyItem);
                    }
                    byteData = new byte[1024];
                    result = fis.read(byteData);
                }
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeHistory(final String searchText) {
        LinearLayout layoutHistory = ButterKnife.findById(getActivity(), R.id.layout_history);
        TextView historyItem = new TextView(getContext());
        historyItem.setText("#" + searchText);
        historyItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchListener.onSearchInput(searchText, token);
                hideKeyboard();
            }
        });
        if (history.add(searchText)) {
            layoutHistory.addView(historyItem);
        }
        try {
            FileOutputStream fos = getActivity().openFileOutput("history", Context.MODE_PRIVATE);
            try {
                for (String item : history) {
                    fos.write(item.getBytes());
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initTwitter() {
        // Init Twitter
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.twitter_key),
                getString(R.string.twitter_secret));
        Fabric.with(getActivity().getApplicationContext(), new Twitter(authConfig));
        String credentials = HttpRequest.Base64.encode(getString(R.string.twitter_key) + ":"
                + getString(R.string.twitter_secret));
        // Get token
        Request.Builder builder = new Request.Builder();
        // builder url
        final String url = "https://api.twitter.com/oauth2/token";
        builder.url(url);
        // builder headers
        final Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.add("Authorization", "Basic " + credentials);
        headersBuilder.add("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        builder.headers(headersBuilder.build());
        // builder post params
        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();
        builder.post(requestBody);
        // Get the Request object
        Request request = builder.build();
        // Make the call to auth and get token
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(getResources().getInteger(R.integer.call_timeout_secs), TimeUnit.SECONDS)
                .writeTimeout(getResources().getInteger(R.integer.call_timeout_secs), TimeUnit.SECONDS)
                .readTimeout(getResources().getInteger(R.integer.call_timeout_secs), TimeUnit.SECONDS)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                showError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // Extract token from the response
                    String jsonString = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            token = jsonObject.getString("access_token");
                            enableSearch();
                        } catch (JSONException e) {
                            showError(e.getMessage());
                        }
                    } else {
                        showError(response.code() + ":" + jsonString);
                    }
                } catch (IOException e) {
                    showError(e.getMessage());
                }
            }
        });
    }

    private void enableSearch() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView header = ButterKnife.findById(getActivity(), R.id.text_view_main_header);
                    header.setVisibility(View.GONE);
                    LinearLayout layout = ButterKnife.findById(getActivity(), R.id.layout_search);
                    layout.setVisibility(View.VISIBLE);
                    EditText editText = ButterKnife.findById(getActivity(), R.id.edit_text_search_input);
                    editText.requestFocus();
                    showKeyboard(editText);
                    readHistory();
                }
            });
        }
    }

    private void showError(final String errorText) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView header = ButterKnife.findById(getActivity(), R.id.text_view_main_header);
                    header.setText(String.format(getString(R.string.result_header_fail), errorText));
                }
            });
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View focusView = getActivity().getCurrentFocus();
        if (focusView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    private void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }
}
