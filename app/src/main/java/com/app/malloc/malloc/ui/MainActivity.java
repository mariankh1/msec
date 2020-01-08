package com.app.malloc.malloc.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import 	androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import 	androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.CpuUsageInfo;
import android.os.HardwarePropertiesManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.transition.Fade;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.app.malloc.malloc.GlideApp;
import com.app.malloc.malloc.ListAdapter;
import com.app.malloc.malloc.ProcFolderParser;
import com.app.malloc.malloc.util.SortEnum;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.app.malloc.malloc.R;
import com.app.malloc.malloc.data.AppItem;
import com.app.malloc.malloc.data.DataManager;
import com.app.malloc.malloc.db.DbIgnoreExecutor;
import com.app.malloc.malloc.service.AlarmService;
import com.app.malloc.malloc.service.AppService;
import com.app.malloc.malloc.util.AppUtil;
import com.app.malloc.malloc.util.PreferenceManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import 	android.media.MediaRecorder;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mSort;
    private Switch mSwitch;
    private TextView mSwitchText;
    private RecyclerView mList;
    private MyAdapter mAdapter;
    private AlertDialog mDialog;
    private SwipeRefreshLayout mSwipe;
    private TextView mSortName;
    private long mTotal;
    private int mDay;
    private PackageManager mPackageManager;
    private TextView mMicrophone;

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private ActivityManager activityManager;
    private TextView mGenralCpuUsage, mTotalMem, mAvailMem;
    private NumberFormat formater = NumberFormat.getNumberInstance(Locale.US);
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] generalInfo = intent.getStringExtra("cpuMem").split(ProcFolderParser.DELIMITER);
            Log.d(LOG_TAG, "received cpu  = " + generalInfo[0]);
          //  mGenralCpuUsage.setText("Total CPU usage: " + generalInfo[0] + "%");
          //  mAvailMem.setText("Available Memory: " + generalInfo[1] + "KB, " + generalInfo[2] + "%");
            mAvailMem.setText("Available Memory: " + generalInfo[1] + "KB");
        }
    };
    Boolean PermissionGranted = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public  void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }else{
            PermissionGranted = true;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // https://guides.codepath.com/android/Shared-Element-Activity-Transition
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Fade(Fade.OUT));
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

            mPackageManager = getPackageManager();

            mSort = findViewById(R.id.sort_group);
            mSortName = findViewById(R.id.sort_name);
            mSwitch = findViewById(R.id.enable_switch);
            mSwitchText = findViewById(R.id.enable_text);
            mAdapter = new MyAdapter();
            mMicrophone = findViewById(R.id.microphone);
            if (getMicrophoneAvailable()) {
                mMicrophone.setText(R.string.microphoneON);
            } else
                mMicrophone.setText(R.string.microphoneOFF);
            mAvailMem = findViewById(R.id.avail_mem);
            //mGenralCpuUsage = findViewById(R.id.general_cpu);
            mTotalMem =  findViewById(R.id.total_mem);
            mList = findViewById(R.id.list);
            mList.setLayoutManager(new LinearLayoutManager(this));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(), DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider, getTheme()));
            mList.addItemDecoration(dividerItemDecoration);
            mList.setAdapter(mAdapter);

            initLayout();
            initEvents();
            initSpinner();
            initSort();

            if (DataManager.getInstance().hasPermission(getApplicationContext())) {
                process();
                startService(new Intent(this, AlarmService.class));
            }

            if (savedInstanceState == null) {
                // getSupportFragmentManager().beginTransaction()
                //          .add(R.id.container, RunningProcessesFragment.newInstance())
                //          .commit();
            }
            // Start the Parser service to get CPU and Memory info from /proc folder
      /*  Permissions.getPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1);*/

     //  HardwarePropertiesManager propertiesManager = (HardwarePropertiesManager)getApplicationContext().getSystemService(Context.HARDWARE_PROPERTIES_SERVICE);
      //  CpuUsageInfo[] cpuUsages = propertiesManager.getCpuUsages();

        //for (CpuUsageInfo i : cpuUsages)
        //    Log.d(">>>>>>>>", ""+i.getTotal());
        Intent intent = new Intent(this, ProcFolderParser.class);
        startService(intent);
        // set text for total memory and threshold, because they are constant and we don't need
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
        mTotalMem.setText("Total memory: " + formater.format(memInfo.totalMem >> 10) + "KB ");

     /*   mTotalMem.setText("Total memory: " + formater.format(memInfo.totalMem >> 10) + "KB   Threshold: "
                + formater.format(memInfo.threshold >> 10) + "KB");
*/



    }

    private void initLayout() {
        mSwipe = findViewById(R.id.swipe_refresh);
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            mSwitchText.setText(R.string.enable_apps_monitoring);
            mSwitch.setVisibility(View.GONE);
            mSort.setVisibility(View.VISIBLE);
            mSwipe.setEnabled(true);
        } else {
            mSwitchText.setText(R.string.enable_apps_monitor);
            mSwitch.setVisibility(View.VISIBLE);
            mSort.setVisibility(View.GONE);
            mSwitch.setChecked(false);
            mSwipe.setEnabled(false);

        }

    }

    private void initSort() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            mSort.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    triggerSort();
                }
            });
        }
    }

    private void triggerSort() {
        mDialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.sort)
                .setSingleChoiceItems(R.array.sort, PreferenceManager.getInstance().getInt(PreferenceManager.PREF_LIST_SORT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PreferenceManager.getInstance().putInt(PreferenceManager.PREF_LIST_SORT, i);
                        process();
                        mDialog.dismiss();
                    }
                })
                .create();
        mDialog.show();
    }

    private void initSpinner() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            Spinner spinner = findViewById(R.id.spinner);
            spinner.setVisibility(View.VISIBLE);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.duration, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mDay != i) {
                        int[] values = getResources().getIntArray(R.array.duration_int);
                        mDay = values[i];
                        process();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    private void initEvents() {
        if (!DataManager.getInstance().hasPermission(getApplicationContext())) {
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        Intent intent = new Intent(MainActivity.this, AppService.class);
                        intent.putExtra(AppService.SERVICE_ACTION, AppService.SERVICE_ACTION_CHECK);
                        startService(intent);
                    }
                }
            });
        }
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                process();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!DataManager.getInstance().hasPermission(getApplicationContext())) {
        //    mSwitch.setChecked(false);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("general"));


    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (DataManager.getInstance().hasPermission(this)) {
            mSwipe.setEnabled(true);
            mSort.setVisibility(View.VISIBLE);
            mSwitch.setVisibility(View.GONE);
            initSpinner();
            initSort();
            process();
        }
    }

    private void process() {
        if (DataManager.getInstance().hasPermission(getApplicationContext())) {
            mList.setVisibility(View.INVISIBLE);
            int sortInt = PreferenceManager.getInstance().getInt(PreferenceManager.PREF_LIST_SORT);
            mSortName.setText(getSortName(sortInt));
            new MyAsyncTask().execute(sortInt, mDay);
        }
    }

    private String getSortName(int sortInt) {
        return getResources().getStringArray(R.array.sort)[sortInt];
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AppItem info = mAdapter.getItemInfoByPosition(item.getOrder());
        switch (item.getItemId()) {
            case R.id.ignore:
                DbIgnoreExecutor.getInstance().insertItem(info);
                process();
                Toast.makeText(this, R.string.ignore_success, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.open:
                startActivity(mPackageManager.getLaunchIntentForPackage(info.mPackageName));
                return true;
            case R.id.more:
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + info.mPackageName));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), 1);
                return true;
            case R.id.sort:
                triggerSort();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(">>>>>>>>", "result code " + requestCode + " " + resultCode);
        if (resultCode > 0) process();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) mDialog.dismiss();
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private List<AppItem> mData;

        MyAdapter() {
            super();
            mData = new ArrayList<AppItem>();

        }

        void updateData(List<AppItem> data) {
            mData = data;

            notifyDataSetChanged();
        }

        AppItem getItemInfoByPosition(int position) {
            if (mData.size() > position) {
                return mData.get(position);
            }
            return null;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            AppItem item = getItemInfoByPosition(position);
            holder.mName.setText(item.mName);
            holder.mUsage.setText(AppUtil.formatMilliSeconds(item.mUsageTime));
            holder.mTime.setText(String.format(Locale.getDefault(),
                    "%s · %d %s",
                    new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date(item.mEventTime)),
                    item.mCount,
                    getResources().getString(R.string.times_only))
            );
            holder.mDataUsage.setText(String.format(Locale.getDefault(), "Wifi:%s Data:%s", AppUtil.humanReadableByteCount(item.mWifi), AppUtil.humanReadableByteCount(item.mMobile)));

            holder.mMemoryUsage.setText(String.format(Locale.getDefault(), "| Memory %s", AppUtil.humanReadableByteCount(item.mMemory)));

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Create a new user with a first and last name
           // Map<String, Object> appdata = new HashMap<>();
            //appdata.put("applabel",item.mName);
           //appdata.put("id",item.pid);
           // appdata.put("cpuusage",item.mUsageTime);
           // appdata.put("date", new Timestamp(new Date()));
           // appdata.put("wifi", item.mWifi);
          //  appdata.put("mobile", item.mWifi);


            // Add a new document with a generated ID
            db.collection("apps").document(item.mName).collection("1")
                    .document(""+new Timestamp(new Date()).getSeconds())
                    .set(item)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("db", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("db", "Error writing document", e);
                        }
                    });

            if (mTotal > 0) {
                holder.mProgress.setProgress((int) (item.mUsageTime * 100 / mTotal));
            } else {
                holder.mProgress.setProgress(0);
            }
            GlideApp.with(MainActivity.this)
                    .load(AppUtil.getPackageIcon(MainActivity.this, item.mPackageName))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.mIcon);
            holder.setOnClickListener(item);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

            private TextView mName;
            private TextView mUsage;
            private TextView mTime;
            private TextView mDataUsage;
            private TextView mMemoryUsage;
            private ImageView mIcon;
            private ProgressBar mProgress;

            MyViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.app_name);
                mUsage = itemView.findViewById(R.id.app_usage);
                mTime = itemView.findViewById(R.id.app_time);
                mDataUsage = itemView.findViewById(R.id.app_data_usage);
                mMemoryUsage= itemView.findViewById(R.id.app_mem_usage);
                mIcon = itemView.findViewById(R.id.app_image);
                mProgress = itemView.findViewById(R.id.progressBar);
                itemView.setOnCreateContextMenuListener(this);
            }

            @SuppressLint("RestrictedApi")
            void setOnClickListener(final AppItem item) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_PACKAGE_NAME, item.mPackageName);
                        intent.putExtra(DetailActivity.EXTRA_DAY, mDay);
                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                makeSceneTransitionAnimation(MainActivity.this, mIcon, "profile");
                        startActivityForResult(intent, 1, options.toBundle());
                    }
                });
            }

            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                int position = getAdapterPosition();
                AppItem item = getItemInfoByPosition(position);
                contextMenu.setHeaderTitle(item.mName);
                contextMenu.add(Menu.NONE, R.id.open, position, getResources().getString(R.string.open));
                if (item.mCanOpen) {
                    contextMenu.add(Menu.NONE, R.id.more, position, getResources().getString(R.string.app_info));
                }
                contextMenu.add(Menu.NONE, R.id.ignore, position, getResources().getString(R.string.ignore));
            }
        }


    }
    public static boolean getMicrophoneAvailable() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
       // recorder.setOutputFile(new File(context.getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());
        boolean available = true;
        try {
            recorder.prepare();
            recorder.start();

        }
        catch (Exception exception) {
            available = false;
        }
        recorder.release();
        return available;
    }
    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Integer, Void, List<AppItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipe.setRefreshing(true);
        }

        @Override
        protected List<AppItem> doInBackground(Integer... integers) {
            return DataManager.getInstance().getApps(getApplicationContext(), integers[0], integers[1]);
        }

        @Override
        protected void onPostExecute(List<AppItem> appItems) {
            mList.setVisibility(View.VISIBLE);
            mTotal = 0;
            for (AppItem item : appItems) {
                if (item.mUsageTime <= 0) continue;
                mTotal += item.mUsageTime;
                item.mCanOpen = mPackageManager.getLaunchIntentForPackage(item.mPackageName) != null;
                item.mMobile =  doInBackground(item.mPackageName)[1];
                item.mWifi =  doInBackground(item.mPackageName)[0];

            }
            mSwitchText.setText(String.format(getResources().getString(R.string.total), AppUtil.formatMilliSeconds(mTotal)));
            mSwipe.setRefreshing(false);
            mAdapter.updateData(appItems);

            if(getMicrophoneAvailable()) {
                mMicrophone.setText(R.string.microphoneON);
            }
            else
                mMicrophone.setText(R.string.microphoneOFF);
        }

        protected Long[] doInBackground( String mPackageName) {
            long totalWifi = 0;
            long totalMobile = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
                int targetUid = AppUtil.getAppUid(getPackageManager(), mPackageName);
                long[] range = AppUtil.getTimeRange(SortEnum.getSortEnum(mDay));
                try {
                    if (networkStatsManager != null) {
                        NetworkStats networkStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, "", range[0], range[1]);
                        if (networkStats != null) {
                            while (networkStats.hasNextBucket()) {
                                NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                                networkStats.getNextBucket(bucket);
                                if (bucket.getUid() == targetUid) {
                                    totalWifi += bucket.getTxBytes() + bucket.getRxBytes();
                                }
                            }
                        }
                        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            NetworkStats networkStatsM = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, tm.getSubscriberId(), range[0], range[1]);
                            if (networkStatsM != null) {
                                while (networkStatsM.hasNextBucket()) {
                                    NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                                    networkStatsM.getNextBucket(bucket);
                                    if (bucket.getUid() == targetUid) {
                                        totalMobile += bucket.getTxBytes() + bucket.getRxBytes();
                                    }
                                }
                            }
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
            return new Long[]{totalWifi, totalMobile};
        }
    }



}
