/**
 * @author Omer Muhammed
 * Copyright 2014 (c) Jawbone. All rights reserved.
 *
 */
package com.jawbone.helloup;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jawbone.upplatformsdk.api.ApiManager;
import com.jawbone.upplatformsdk.utils.UpPlatformSdkConstants;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Activity that provides a list view of all the API calls available
 * for UP Platform
 */
public class UpApiListActivity extends Activity {

    private static final String TAG = UpApiListActivity.class.getSimpleName();

    public static String str ;
    public static String str_step;

    private String mAccessToken;
    private String mClientSecret;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setBackgroundDrawableResource(R.color.white);

        Intent intent = getIntent();
        if (intent != null) {
            mClientSecret = intent.getStringExtra(UpPlatformSdkConstants.CLIENT_SECRET);
        }

/*
        ListView listView = getListView();
        LayoutInflater inflater = LayoutInflater.from(this);
        TextView header = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, listView, false);
        header.setText("List of UP Platform API Calls");
        header.setTextColor(Color.WHITE);
        header.setBackgroundColor(Color.DKGRAY);
        header.setGravity(Gravity.CENTER);
        listView.addHeaderView(header);

        String[] displayStrings = new String[UpPlatformSdkConstants.RestApiRequestType.size];
        int index = 0;
        for (UpPlatformSdkConstants.RestApiRequestType r: UpPlatformSdkConstants.RestApiRequestType.values()) {
            displayStrings[index] = r.toString();
            index++;
        }

        setListAdapter(new ArrayAdapter<String>(this, R.layout.up_api_list, displayStrings));

        ColorDrawable sage = new ColorDrawable(this.getResources().getColor(R.color.black_overlay));
        listView.setDivider(sage);
        listView.setDividerHeight(1);
*/
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAccessToken = preferences.getString(UpPlatformSdkConstants.UP_PLATFORM_ACCESS_TOKEN, null);

        if (mAccessToken != null) {
            ApiManager.getRequestInterceptor().setAccessToken(mAccessToken);
            //listView.setOnItemClickListener(restApiListener());
        }

        Log.e(TAG, "making Get Move Events List api call ...");
        ApiManager.getRestApiInterface().getMoveEventsList(
                UpPlatformSdkConstants.API_VERSION_STRING,
                getMoveEventsListRequestParams(),
                genericCallbackListener);

        setContentView(R.layout.new_activity);
        TextView tv = (TextView)findViewById(R.id.marquee);
        tv.setSelected(true);
        //tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
    }

    public void start_countdown(View view){
        /*
        TextView tv = (TextView)findViewById(R.id.text);
        tv.setText(str_step);
        */
        Log.e(TAG, "making Get Move Events List api call ...");
        ApiManager.getRestApiInterface().getMoveEventsList(
                UpPlatformSdkConstants.API_VERSION_STRING,
                getMoveEventsListRequestParams(),
                genericCallbackListener);

        final int begin_step = Integer.parseInt(str_step);
        final EditText etm = (EditText)findViewById(R.id.editText_minute);
        final EditText ets = (EditText)findViewById(R.id.editText_second);
        int minute, second;
        if(etm.getText().toString().length() != 0)
            minute = Integer.parseInt(etm.getText().toString());
        else
            minute = 0;
        if(ets.getText().toString().length() != 0)
            second = Integer.parseInt(ets.getText().toString());
        else
            second = 0;
        CountDownTimer cdt = new CountDownTimer(minute*60000 + second*1000, 1000) {
            @Override
            public void onTick(long l) {
                //etm.setText(Integer.toString((int)l/60000));
                etm.setText(String.valueOf(l/60000));
                ets.setText(String.valueOf((l%60000)/1000));
                if(l/60000 == 0 && (l%60000)/1000 == 3){
                    Log.e(TAG, "making Get Move Events List api call ...");
                    ApiManager.getRestApiInterface().getMoveEventsList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getMoveEventsListRequestParams(),
                            genericCallbackListener);
                }
            }

            @Override
            public void onFinish() {
                ets.setText(String.valueOf(0));
                int end_step = Integer.parseInt(str_step);
                if(end_step == begin_step){
                    /*Toast.makeText(getApplicationContext(), "雷茲非常平衡", Toast.LENGTH_LONG).show();*/
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpApiListActivity.this);
                    builder.setTitle("時間到囉");
                    builder.setMessage("肥宅該起來動一動囉");
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // auto return
                                }
                            }
                    );
                    builder.show();
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone ring = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    ring.play();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpApiListActivity.this);
                    builder.setTitle("好棒棒");
                    builder.setMessage("你走了" + String.valueOf(end_step-begin_step) + "步，消耗了約"
                                    + String.valueOf((end_step-begin_step)/20) + "大卡!");
                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // auto return
                                }
                            }
                    );
                    builder.show();
                }
            }
        }.start();
    }


    /**
     * Listener for the API listview, note that some API calls will not work because:
     * 1. They need a valid xid, and 2. we cannot delete what we didn't create, that
     * is, we cannot delete a meal that we did not create.
     */

    private OnItemClickListener restApiListener() {
        return new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                } else {
                    // list header is position 0, so decrement to keep it consistent
                    position = position - 1;
                }
                Log.e(TAG, "position clicked is :" + position);
                UpPlatformSdkConstants.RestApiRequestType apiRequestType = UpPlatformSdkConstants.RestApiRequestType.values()[position];
                switch (apiRequestType) {/*
                    case GET_MEALS_EVENTS_LIST:
                        Log.e(TAG, "making Get Meal Events List api call ...");
                        ApiManager.getRestApiInterface().getMealEventsList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getMealEventsListRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_MEALS_EVENT:
                        Log.e(TAG, "making Get Meal Event api call ...");
                        ApiManager.getRestApiInterface().getMealEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "JtN269m6S_xmX72fwD63cg", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case DELETE_MEAL:
                        Log.e(TAG, "making Delete Meal api call ...");
                        // note, you can only delete meals that you created
                        // so first create it then delete
                        ApiManager.getRestApiInterface().deleteMealEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case CREATE_MEAL:
                        Log.e(TAG, "making Create Meal api call ...");
                        ApiManager.getRestApiInterface().createMealEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                                getCreateOrUpdateMealEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case UPDATE_MEAL:
                        Log.e(TAG, "making Update Meal api call ...");
                        ApiManager.getRestApiInterface().updateMealEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "JtN269m6S_xmX72fwD63cg", //hardcoded value, should be dynamic
                                getCreateOrUpdateMealEventRequestParams(),
                            genericCallbackListener);
                        break;

                    case GET_MOVES_EVENTS_LIST:
                        Log.e(TAG, "making Get Move Events List api call ...");
                        ApiManager.getRestApiInterface().getMoveEventsList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getMoveEventsListRequestParams(),
                            genericCallbackListener);
                        Intent intent = new Intent(UpApiListActivity.this, TestNewActivity.class);
                        startActivity(intent);
                        break;

                    case GET_MOVES_EVENT:
                        Log.e(TAG, "making Get Move Event api call ...");
                        ApiManager.getRestApiInterface().getMoveEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "JtN269m6S_xR5T-blbnJBA", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_MOVES_GRAPH:
                        Log.e(TAG, "making Get Move Graph api call ...");
                        ApiManager.getRestApiInterface().getMoveGraph(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "wZ3pxuSAHA9mnOxjz3yw5w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_MOVES_TICKS:
                        Log.e(TAG, "making Get Move Ticks api call ...");
                        ApiManager.getRestApiInterface().getMoveTicks(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "wZ3pxuSAHA9mnOxjz3yw5w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_CUSTOM_EVENTS_LIST:
                        Log.e(TAG, "making Get Custom Events List api call ...");
                        ApiManager.getRestApiInterface().getCustomEventsList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCustomEventsListRequestParams(),
                            genericCallbackListener);
                        break;
                    case CREATE_CUSTOM_EVENT:
                        Log.e(TAG, "making Create Custom Event api call ...");
                        ApiManager.getRestApiInterface().createCustomEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateOrUpdateCustomEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case UPDATE_CUSTOM_EVENT:
                        Log.e(TAG, "making Update Custom Event api call ...");
                        ApiManager.getRestApiInterface().updateCustomEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateOrUpdateCustomEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case DELETE_CUSTOM_EVENT:
                        Log.e(TAG, "making Delete Custom Event api call ...");
                        ApiManager.getRestApiInterface().deleteCustomEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_WORKOUTS_EVENTS_LIST:
                        Log.e(TAG, "making Get Workout Events List api call ...");
                        ApiManager.getRestApiInterface().getWorkoutEventList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getWorkoutEventsListRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_WORKOUTS_EVENT:
                        Log.e(TAG, "making Get Workout Event api call ...");
                        ApiManager.getRestApiInterface().getWorkoutEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "JtN269m6S_xYwnSu7WQdfA", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_WORKOUTS_GRAPH:
                        Log.e(TAG, "making Get Workout Graph api call ...");
                        ApiManager.getRestApiInterface().getWorkoutGraph(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_WORKOUTS_TICKS:
                        Log.e(TAG, "making Get Workout Ticks api call ...");
                        ApiManager.getRestApiInterface().getWorkoutTicks(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case CREATE_WORKOUT_EVENT:
                        Log.e(TAG, "making Create Workout Event api call ...");
                        ApiManager.getRestApiInterface().createWorkoutEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateOrUpdateWorkoutEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case UPDATE_WORKOUT_EVENT:
                        Log.e(TAG, "making Update Workout Event api call ...");
                        ApiManager.getRestApiInterface().updateWorkoutEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateOrUpdateWorkoutEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case DELETE_WORKOUT_EVENT:
                        Log.e(TAG, "making Delete Workout Event api call ...");
                        ApiManager.getRestApiInterface().deleteWorkoutEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_SLEEP_EVENTS_LIST:
                        Log.e(TAG, "making Get Sleep Events List api call ...");
                        ApiManager.getRestApiInterface().getSleepEventsList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getSleepEventsListRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_SLEEP_EVENT:
                        Log.e(TAG, "making Get Sleep Event api call ...");
                        ApiManager.getRestApiInterface().getSleepEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "Zmz9yIE9kk0zi4-n8juddg", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_SLEEP_GRAPH:
                        Log.e(TAG, "making Get Sleep Graph api call ...");
                        ApiManager.getRestApiInterface().getSleepGraph(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_SLEEP_TICKS:
                        Log.e(TAG, "making Get Sleep Ticks api call ...");
                        ApiManager.getRestApiInterface().getSleepPhases(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case CREATE_SLEEP_EVENT:
                        Log.e(TAG, "making Create Sleep Event api call ...");
                        ApiManager.getRestApiInterface().createSleepEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateSleepEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case DELETE_SLEEP_EVENT:
                        Log.e(TAG, "making Delete Sleep Event api call ...");
                        ApiManager.getRestApiInterface().deleteSleepEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_BODY_EVENTS_LIST:
                        Log.e(TAG, "making Get Body Events List api call ...");
                        ApiManager.getRestApiInterface().getBodyEventsList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getBodyEventsListRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_BODY_EVENT:
                        Log.e(TAG, "making Get Body Event api call ...");
                        ApiManager.getRestApiInterface().getBodyEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "JtN269m6S_yjZabtdYhsJQ", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case CREATE_BODY_EVENT:
                        Log.e(TAG, "making Create Body Event api call ...");
                        ApiManager.getRestApiInterface().createBodyEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateBodyEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case DELETE_BODY_EVENT:
                        Log.e(TAG, "making Delete Body Event api call ...");
                        ApiManager.getRestApiInterface().deleteBodyEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_BAND_EVENTS:
                        Log.e(TAG, "making Get Band Events api call ...");
                        ApiManager.getRestApiInterface().getBandEvents(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getBandEventsRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_GOALS:
                        Log.e(TAG, "making Get Users Goals api call ...");
                        ApiManager.getRestApiInterface().getUsersGoals(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            genericCallbackListener);
                        break;
                    case CREATE_OR_UPDATE_GOALS:
                        Log.e(TAG, "making Create or Update Users Goals api call ...");
                        ApiManager.getRestApiInterface().createOrUpdateUsersGoals(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateOrUpdateUsersGoalsRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_MOOD_EVENTS_LIST:
                        Log.e(TAG, "making Get Mood Events List api call ...");
                        ApiManager.getRestApiInterface().getMoodEventsList(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "20140908",
                            genericCallbackListener);
                        break;
                    case GET_MOOD_EVENT:
                        Log.e(TAG, "making Get Mood Event api call ...");
                        ApiManager.getRestApiInterface().getMoodEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case CREATE_MOOD_EVENT:
                        Log.e(TAG, "making Create Mood Event api call ...");
                        ApiManager.getRestApiInterface().createMoodEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getCreateMoodEventRequestParams(),
                            genericCallbackListener);
                        break;
                    case DELETE_MOOD_EVENT:
                        Log.e(TAG, "making Delete Mood Event api call ...");
                        ApiManager.getRestApiInterface().deleteMoodEvent(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            "O-IjYEIQFz6dU0FWTA0U-w", //hardcoded value, should be dynamic
                            genericCallbackListener);
                        break;
                    case GET_REFRESH_TOKEN:
                        Log.e(TAG, "making Get Refresh Token api call ...");
                        if (mClientSecret != null) {
                            ApiManager.getRestApiInterface().getRefreshToken(
                                UpPlatformSdkConstants.API_VERSION_STRING,
                                mClientSecret,
                                genericCallbackListener);
                        } else {
                            Log.e(TAG, "client secret is null, so api call is aborted..");
                        }
                        break;
                    case GET_SETTINGS:
                        Log.e(TAG, "making Get User Settings api call ...");
                        ApiManager.getRestApiInterface().getUserSettings(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            genericCallbackListener);
                        break;
                    case GET_TIME_ZONE:
                        Log.e(TAG, "making Get Time Zone api call ...");
                        ApiManager.getRestApiInterface().getTimeZone(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getTimeZoneRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_TRENDS:
                        Log.e(TAG,  "making Get Trends api call ...");
                        ApiManager.getRestApiInterface().getTrends(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            getTrendsRequestParams(),
                            genericCallbackListener);
                        break;
                    case GET_USER:
                        Log.e(TAG, "making Get User api call ...");
                        ApiManager.getRestApiInterface().getUser(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            genericCallbackListener);
                        break;
                    case GET_USERS_FRIENDS:
                        Log.e(TAG, "making Get Users Friends api call ...");
                        ApiManager.getRestApiInterface().getUsersFriends(
                            UpPlatformSdkConstants.API_VERSION_STRING,
                            genericCallbackListener);
                        break;
                        */
                    default:
                        Log.e(TAG, "api endpoint not yet defined, position of clicked is:" + position );
                        break;
                }
            }
        };
    }

    //TODO the callbacks are not yet backed by data model, but will get json response,
    //TODO which for now is logged to console
    private Callback genericCallbackListener = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            Log.e(TAG,  "api call successful, json output: " + o.toString());
            str = o.toString() ;
            str_step = str.substring(str.indexOf("了")+1, str.indexOf("步"));
            str_step = str_step.replaceAll(",","");
            str_step = str_step.replaceAll(" ","");
            //Toast.makeText(getApplicationContext(), str_step, Toast.LENGTH_LONG).show();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG,  "api call failed, error message: " + retrofitError.getMessage());
            Toast.makeText(getApplicationContext(), retrofitError.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    /**
     * The following are some handy methods to wrap up the parameters posted
     * to server. Retrofit allows them to be sent as Map<String, Object> and
     * we use that.
     */
    private static HashMap<String, Object> getTrendsRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        queryHashMap.put("end_date", "<insert-date>");
//        queryHashMap.put("bucket_size", 50);
//        queryHashMap.put("num_buckets", 10);

        return queryHashMap;
    }

    private static HashMap<String, Integer> getTimeZoneRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("timestamp", "<insert-time>");

        return queryHashMap;
    }

    private static HashMap<String, Object> getCreateMoodEventRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        queryHashMap.put("title", "<insert-title>");
//        queryHashMap.put("sub_tye", 1);
//        queryHashMap.put("time_created", 1409967653);
//        queryHashMap.put("tz", null);
//        queryHashMap.put("share", false);

        return queryHashMap;
    }

    private static HashMap<String, Object> getCreateOrUpdateUsersGoalsRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        float placeHolder = new Float(0);
//        queryHashMap.put("move_steps", "<insert-steps>");
//        queryHashMap.put("sleep_total", "<insert-sleep>");
//        queryHashMap.put("body_weight", placeHolder);
//        queryHashMap.put("body_weight_intent", "<insert-intent>");

        return queryHashMap;
    }

    private static HashMap<String, Integer> getBandEventsRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("created_after", "<insert-time>");

        return queryHashMap;
    }

    private static HashMap<String, Object> getCreateBodyEventRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        float placeHolder = new Float(0);
//        queryHashMap.put("title", "<insert-title>");
//        queryHashMap.put("weight", placeHolder);
//        queryHashMap.put("body_fat", placeHolder);
//        queryHashMap.put("lean_mass", placeHolder);
//        queryHashMap.put("bmi", placeHolder);
//        queryHashMap.put("note", "<insert-note>");
//        queryHashMap.put("time_created", 1409967653);
//        queryHashMap.put("tz", null);
//        queryHashMap.put("share", false);

        return queryHashMap;
    }

    private static HashMap<String, Integer> getBodyEventsListRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("page_token", "<insert-page-token>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("updated_after", "<insert-time>");
//        queryHashMap.put("limit", "<insert-limit>");

        return queryHashMap;
    }

    private static HashMap<String, Object> getCreateSleepEventRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        queryHashMap.put("time_created", 1);
//        queryHashMap.put("time_completed", 1);
//        queryHashMap.put("tz", null);
//        queryHashMap.put("share", false);

        queryHashMap.put("time_created", 1409967653);
        queryHashMap.put("time_completed", 1409967655);
        return queryHashMap;
    }

    private static HashMap<String, Integer> getSleepEventsListRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("page_token", "<insert-page-token>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("updated_after", "<insert-time>");

        return queryHashMap;
    }

    private static HashMap<String, Object> getCreateOrUpdateWorkoutEventRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        File photo = new File(""); //path to image file
//        TypedFile typedFile = new TypedFile("application/octet-stream", photo);
//        float placeHolder = new Float(0);
//        queryHashMap.put("sub_type", 1);
//        queryHashMap.put("image_url", URI.create("<insert-uri>"));
//        queryHashMap.put("time_created", 1);
//        queryHashMap.put("time_completed", 1);
//        queryHashMap.put("place_lat", placeHolder);
//        queryHashMap.put("place_lon", placeHolder);
//        queryHashMap.put("place_acc", placeHolder);
//        queryHashMap.put("place_name", null);
//        queryHashMap.put("tz", null);
//        queryHashMap.put("share", false);
//        queryHashMap.put("calories", 100);
//        queryHashMap.put("distance", 700);
//        queryHashMap.put("intensity", 5);

        queryHashMap.put("sub_type", 1);
        return queryHashMap;
    }

    private static HashMap<String, Integer> getWorkoutEventsListRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("page_token", "<insert-page-token>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("updated_after", "<insert-time>");
//        queryHashMap.put("limit", "<insert-limit>");

        return queryHashMap;
    }

    private HashMap<String, Object> getCreateOrUpdateCustomEventRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

        //uncomment to add as needed parameters
//        Object jsonObject = null;
//        float placeHolder = new Float(0);
//        queryHashMap.put("title", "<insert-title>");
//        queryHashMap.put("verb", "<insert-verb>");
//        queryHashMap.put("attributes", jsonObject);
//        queryHashMap.put("image_url", URI.create("<insert-uri>"));
//        queryHashMap.put("place_lat", placeHolder);
//        queryHashMap.put("place_lon", placeHolder);
//        queryHashMap.put("place_acc", placeHolder);
//        queryHashMap.put("place_name", null);
//        queryHashMap.put("time_created", 1);
//        queryHashMap.put("tz", null);
//        queryHashMap.put("share", false);

        return queryHashMap;
    }

    private static HashMap<String, Integer> getCustomEventsListRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("page_token", "<insert-page-token>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("updated_after", "<insert-time>");
//        queryHashMap.put("limit", "<insert-limit>");

        return queryHashMap;
    }

    private static HashMap<String, Integer> getMoveEventsListRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("page_token", "<insert-page-token>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("updated_after", "<insert-time>");

        return queryHashMap;
    }

    private static HashMap<String, Object> getCreateOrUpdateMealEventRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        File photo = new File(""); //path to image file
//        TypedFile typedFile = new TypedFile("application/octet-stream", photo);
//        float placeHolder = new Float(0);
//        queryHashMap.put("note", "<insert-title>");
//        queryHashMap.put("sub_type", 1);
//        queryHashMap.put("image_url", URI.create("<insert-uri>"));
//        queryHashMap.put("photo", typedFile);
//        queryHashMap.put("place_lat", placeHolder);
//        queryHashMap.put("place_lon", placeHolder);
//        queryHashMap.put("place_acc", placeHolder);
//        queryHashMap.put("time_created", 1);
//        queryHashMap.put("place_name", null);
//        queryHashMap.put("tz", null);
//        queryHashMap.put("share", false);

        queryHashMap.put("note", "Create Meal 1");
        queryHashMap.put("sub_type", 3);
        return queryHashMap;
    }

    private static HashMap<String, Integer> getMealEventsListRequestParams() {
        HashMap<String, Integer> queryHashMap = new HashMap<String, Integer>();

        //uncomment to add as needed parameters
//        queryHashMap.put("date", "<insert-date>");
//        queryHashMap.put("page_token", "<insert-page-token>");
//        queryHashMap.put("start_time", "<insert-time>");
//        queryHashMap.put("end_time", "<insert-time>");
//        queryHashMap.put("updated_after", "<insert-time>");

        return queryHashMap;
    }
}
