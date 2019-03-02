package com.mta.location;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.mta.location.util.Cache;

import java.util.Date;

import static com.mta.location.util.Config.TAG;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocationUpdateJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "Job start");
        startLocationUpdate(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "Job stop");
        Context context = getApplicationContext();
        Location.getInstance(context).stopLocationUpdates(context);
        return false;
    }

    private static void startLocationUpdate(Context context) {
        Location instance = Location.getInstance(context);
        if(Location.isLocationUpdating(context)) {
            if (!instance.isRunning()) {
                instance.startLocationUpdates(context);
            }
        }
    }

    public static void scheduleJob(Context context) {
        Location.setLastUpdated(context, new Date().getTime());
        Cache.put(context, Cache.IS_LOCATION_UPDATING, true);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(context, LocationUpdateJob.class);
        JobInfo jobInfoObj = new JobInfo.Builder(1, componentName)
                .setPeriodic(10*60*1000).build();
        if (jobScheduler != null) {
            int ret = jobScheduler.schedule(jobInfoObj);

            if (ret == JobScheduler.RESULT_SUCCESS) {
                Log.e(TAG, "Job scheduled successfully");
            } else {
                Log.e(TAG, "Job scheduling failed");
            }
        }
        startLocationUpdate(context);
    }
}
