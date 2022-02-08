package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    SwipeRefreshLayout swipeContainer;

    Toolbar toolbar;

    EndlessRecyclerViewScrollListener scrollListener;

    public static final String TAG = "TimeLineActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.twitterToolbar);

        setSupportActionBar(toolbar);

        client = TwitterApplication.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                Log.i(TAG, "Fetched new data");
                populateHomeTimeline();
            }
        });

        //Find recycler view
        rvTweets = findViewById(R.id.rvTweets);
        //Init the list of tweets and adaptor

        tweets = new ArrayList<>();
        tweetsAdapter = new TweetsAdapter(this, tweets);

        //Recycler view setup : Layout manager and the adapter

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(layoutManager);

        rvTweets.setAdapter(tweetsAdapter);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager)
        {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view)
            {
                Log.i(TAG, "onLoadMore" + page);
                loadMoreData();
            }
        };

        rvTweets.addOnScrollListener(scrollListener);

        populateHomeTimeline();
    }

    private void loadMoreData()
    {
        // Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json)
            {
                Log.i(TAG, "On Success for loadMoreData" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try
                {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    tweetsAdapter.addAll(tweets);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable)
            {
                Log.e(TAG, "On failure for loadMoreData", throwable);
            }
        }, tweets.get(tweets.size() - 1).id);
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.

        //  --> Deserialize and construct new model objects from the API response

        //  --> Append the new data objects to the existing set of items inside the array of items

        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
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
                    tweetsAdapter.clear();
                    tweetsAdapter.addAll(Tweet.fromJsonArray(jsonArray));
                    swipeContainer.setRefreshing(false);
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