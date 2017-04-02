package com.example.jesusruizcarmena.hashtagapp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.jesusruizcarmena.hashtagapp.R;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by jesusruizcarmena on 3/26/17.
 */

public class ResultFragment extends Fragment {

    private static final String ARG_SEARCH_TEXT = "arg_search_text";
    private static final String ARG_TOKEN = "arg_token";
    private String searchText;
    private String token;
    private Timer refreshTimer;

    @BindView(R.id.progress_bar_result)
    ProgressBar progressBar;


    public ResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ResultFragment.
     */
    public static ResultFragment newInstance(String searchText, String token) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_TEXT, searchText);
        args.putString(ARG_TOKEN, token);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchText = getArguments().getString(ARG_SEARCH_TEXT);
            token = getArguments().getString(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        ButterKnife.bind(this, view);
        setProgressBarLoading(true);
        TextView header = ButterKnife.findById(view, R.id.text_view_result_header);
        header.setText(String.format(getString(R.string.result_header_ok), searchText.trim()));
        refreshTimer = new Timer();
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getTweets(searchText, token);
            }
        }, 0, 1000 * getResources().getInteger(R.integer.result_refresh_timeout_secs));
        return view;
    }

    @Override
    public void onDestroyView() {
        setProgressBarLoading(false);
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
        super.onDestroyView();
    }

    private void getTweets(final String hashTag, String token) {
        // Request Tweets
        Request.Builder builder = new Request.Builder();
        // Builder url
        String url = "https://api.twitter.com/1.1/search/tweets.json?q=%23" + hashTag + "&result_type=popular";
        builder.url(url)
                .get();
        // Builder headers
        final Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.add("Authorization", "Bearer " + token);
        builder.headers(headersBuilder.build());
        // Get the Request object
        Request request = builder.build();
        // Make the call
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(getResources().getInteger(R.integer.call_timeout_secs), TimeUnit.SECONDS)
                .writeTimeout(getResources().getInteger(R.integer.call_timeout_secs), TimeUnit.SECONDS)
                .readTimeout(getResources().getInteger(R.integer.call_timeout_secs), TimeUnit.SECONDS)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setProgressBarLoading(false);
                e.printStackTrace();
                showError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setProgressBarLoading(false);
                        }
                    });
                }
                try {
                    String jsonString = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            // Extract statuses from the response
                            JSONObject jsonObject = new JSONObject(jsonString);
                            JSONArray jsonArray = jsonObject.getJSONArray("statuses");
                            // For each status, extract its tweet id and request the related tweet view
                            final LinearLayout layoutResult = ButterKnife.findById(getActivity(), R.id.layout_result);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layoutResult.removeAllViewsInLayout();
                                    }
                                });
                            }
                            for (int i = 0; i < 10 && i < jsonArray.length(); i++) {
                                JSONObject tweet = jsonArray.getJSONObject(i);
                                TweetUtils.loadTweet(tweet.getLong("id"),
                                        new com.twitter.sdk.android.core.Callback<Tweet>() {
                                            @Override
                                            public void success(Result<Tweet> result) {
                                                // Bind the tweet view we just received to our UI
                                                layoutResult.addView(new TweetView(getContext(), result.data));
                                            }

                                            @Override
                                            public void failure(TwitterException exception) {
                                                exception.printStackTrace();
                                            }
                                        });
                            }
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

    private void showError(final String errorText) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView subHeader = ButterKnife.findById(getActivity(), R.id.text_view_result_sub_header);
                    subHeader.setText(String.format(getString(R.string.result_header_fail), errorText));
                }
            });
        }
    }

    private void setProgressBarLoading(final boolean isLoading) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isLoading) {
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
