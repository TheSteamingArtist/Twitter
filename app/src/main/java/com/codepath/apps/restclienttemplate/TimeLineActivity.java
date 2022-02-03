package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimeLineActivity extends AppCompatActivity {

    TwitterClient client;

    RecyclerView rvTweets;

    List<Tweet> tweets;

    TweetsAdapter tweetsAdapter;

    public static final String TAG = "TimeLineActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = TwitterApplication.getRestClient(this);

        //Find recycler view
        rvTweets = findViewById(R.id.rvTweets);
        //Init the list of tweets and adaptor

        tweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(this, tweets);

        //Recycler view setup : Layout manager and the adapter

        rvTweets.setLayoutManager(new LinearLayoutManager(this));

        rvTweets.setAdapter(tweetsAdapter);

        populateHomeTimeline();
    }

    private void populateHomeTimeline()
    {
        client.getHomeTimeline(new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json)
            {
                Log.i(TAG , "Success");

                JSONArray jsonArray = json.jsonArray;
                try
                {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    tweetsAdapter.notifyDataSetChanged();
                }
                catch (JSONException e)
                {
                    Log.e(TAG, "Json Exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable)
            {
                Log.e(TAG , "Failure", throwable);
            }
        });
    }
}