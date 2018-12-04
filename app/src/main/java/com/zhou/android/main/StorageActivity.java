package com.zhou.android.main;

import android.app.usage.ExternalStorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zhou.android.R;
import com.zhou.android.common.BaseActivity;
import com.zhou.android.common.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 存储查询
 * Created by Administrator on 2018/11/30.
 */

public class StorageActivity extends BaseActivity {

    private final static String TAG = "storage";
    private final static int RequestCode = 0x1001;
    private TextView text;

    private PackageObserver packageObserver;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_storage);
    }

    @Override
    protected void init() {
        text = findViewById(R.id.text);
    }

    @Override
    protected void addListener() {

    }

    public void onClick(View v) {

        checkExceptSystemCapacity();
        append("================================");
        getAppSize();
    }

    /**
     * 除去系统后的内存大小
     */
    private void checkExceptSystemCapacity() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long totalSize = statFs.getTotalBytes();
        long availableSize = statFs.getAvailableBytes();

        append("total = " + getUnit(totalSize));
        append("availableSize = " + getUnit(availableSize));
    }

    private String[] units = {"B", "KB", "MB", "GB", "TB"};

    /**
     * 进制 1024，像Android系统的内存显示进制是使用1000
     */
    private String getUnit(float size) {
        return getUnit(size, 1024);
    }

    private String getUnit(float size, float base) {
        int index = 0;
        while (size > base && index < 4) {
            size = size / base;
            index++;
        }
        return String.format(Locale.getDefault(), " %.2f %s", size, units[index]);
    }

    private void append(String _text) {
        text.append(_text);
        text.append("\n");
    }

    private class PackageObserver extends IPackageStatsObserver.Stub {

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {

            Message msg = handler.obtainMessage();
            msg.obj = pStats.toString();
            msg.sendToTarget();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            append((String) msg.obj);
        }
    };

    /**
     * 获取总共容量大小，包括系统大小
     */
    public long getTotalSize(String fsUuid) {
        try {
            UUID id;
            if (fsUuid == null) {
                id = StorageManager.UUID_DEFAULT;
            } else {
                id = UUID.fromString(fsUuid);
            }
            StorageStatsManager stats = getSystemService(StorageStatsManager.class);
            return stats.getTotalBytes(id);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取存储块id
     *
     * @param fsUuid
     * @return
     */

    private UUID getUuid(String fsUuid) {
        UUID id;
        if (fsUuid == null) {
            id = StorageManager.UUID_DEFAULT;
        } else {
            id = UUID.fromString(fsUuid);
        }
        return id;
    }

    /**
     * 查询其他目录的大小
     *
     * @param fsUuid
     * @param sharedUuid
     */
    private void measure(UUID fsUuid, UUID sharedUuid) {
        StorageStatsManager stats = getSystemService(StorageStatsManager.class);
        try {
            try {
                long total = stats.getTotalBytes(fsUuid);
                long free = stats.getFreeBytes(fsUuid);
                Log.d(TAG, "total = " + getUnit(total, 1000) + " ,free = " + getUnit(free, 1000));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (sharedUuid == null) {
                Log.e(TAG, "不可读");
                return;
            }

//            Method getUsers = userManager.getClass().getDeclaredMethod("getUsers");
//            List<Object> users = (List<Object>) getUsers.invoke(userManager);//SystemApi 权限限制，系统应用，除非 push 到 data/app 目录下

//            for (Object user : users) {
//                    Field id = user.getClass().getField("id");
//                    int _id = id.getInt(user);

            int uid = android.os.Process.myUid();//获取当前用户id
            ExternalStorageStats mState = stats.queryExternalStatsForUser(fsUuid, UserHandle.getUserHandleForUid(uid));
            //因为用户只有一个，所以直接取值
            long app = mState.getAppBytes();
            long audio = mState.getAudioBytes();
            long image = mState.getImageBytes();
            long video = mState.getVideoBytes();
            Log.e(TAG, String.format(Locale.getDefault(), "app = %s ,audio = %s ,image = %s ,video = %s",
                    getUnit(app, 1000), getUnit(audio, 1000), getUnit(image, 1000), getUnit(video, 1000)));
            Message msg = handler.obtainMessage();
            msg.obj = String.format(Locale.getDefault(), "app = %s\naudio = %s\nimage = %s\nvideo = %s",
                    getUnit(app, 1000), getUnit(audio, 1000), getUnit(image, 1000), getUnit(video, 1000));
            msg.sendToTarget();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getAppSize() {
        boolean flag = false;
        Method[] methods = PackageManager.class.getMethods();
        for (Method m : methods) {
            if ("getPackageSizeInfo".equals(m.getName())) {
                Class<?>[] classes = m.getParameterTypes();
                if (classes.length == 2 || classes.length == 3) {
                    flag = true;
                    break;
                }
            }
        }
        if (flag) {
            useIPackageStatsObserver();
        } else {
            useStorageManager();
        }
    }

    private void useIPackageStatsObserver() {
        //需要权限 android.permission.GET_PACKAGE_SIZE
        Log.d(TAG, "useIPackageStatsObserver");
        packageObserver = new PackageObserver();
        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageManager pm = getPackageManager();
                List<PackageInfo> packageList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
                boolean fun1 = false, fun2 = false;
                for (PackageInfo info : packageList) {
                    if (fun1 && fun2)
                        break;
                    if (!fun1) {
                        try {
                            Method myUserId = UserHandle.class.getDeclaredMethod("myUserId");
                            int uid = (Integer) myUserId.invoke(pm, new Object[]{});

                            Method getPackageSizeInfo = pm.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, int.class, IPackageStatsObserver.class);
                            getPackageSizeInfo.invoke(pm, info.packageName, uid, packageObserver);
                            continue;
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                            fun1 = true;
                        }
                    }

                    if (!fun2) {
                        try {
                            Method getPackageSizeInfo = pm.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
                            getPackageSizeInfo.invoke(pm, info.packageName, packageObserver);
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                            fun2 = true;
                        }
                    }
                }
                if (fun1 && fun2) {
                    Log.e(TAG, "useIPackageStatsObserver is failed,try useStorageManger.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            useStorageManager();
                        }
                    });
                }
            }
        }
        ).start();
    }

    private void useStorageManager() {

        Log.d(TAG, "useStorageManager");
        //这里的权限检查有问题，模拟器一直返回 -1
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            if (checkSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PACKAGE_USAGE_STATS)) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.PACKAGE_USAGE_STATS}, RequestCode);
//                } else {
//
//                }
//            }
//        }

        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);

        try {
            Method getVolumes = storageManager.getClass().getDeclaredMethod("getVolumes");
            List<Object> getVolumeInfo = (List<Object>) getVolumes.invoke(storageManager);
            long total = 0L, used = 0L;
            for (Object obj : getVolumeInfo) {

                Field getType = obj.getClass().getField("type");
                int type = getType.getInt(obj);
                Log.d(TAG, "type: " + type);
                if (type == 1) {//TYPE_PRIVATE
                    Method getFsUuid = obj.getClass().getDeclaredMethod("getFsUuid");
                    String fsUuid = (String) getFsUuid.invoke(obj);
                    long totalSize = getTotalSize(fsUuid);

                    Method isMountedReadable = obj.getClass().getDeclaredMethod("isMountedReadable");
                    boolean readable = (boolean) isMountedReadable.invoke(obj);
                    if (readable) {
                        Method file = obj.getClass().getDeclaredMethod("getPath");
                        File f = (File) file.invoke(obj);

                        Log.d(TAG, "SD totalSize " + getUnit(totalSize, 1000) + " , 总共 = " + getUnit(f.getTotalSpace(), 1000) + ", 可用 = " + getUnit(f.getFreeSpace(), 1000));

                        used += totalSize - f.getFreeSpace();
                        total += totalSize;
                    }
                    Message msg = handler.obtainMessage();
                    msg.obj = "totalSize = " + getUnit(totalSize) + " ,used = " + getUnit(used) + " ,free = " + getUnit(totalSize - used);
                    msg.sendToTarget();

                    Method getId = obj.getClass().getDeclaredMethod("getId");
                    String id = (String) getId.invoke(obj);
                    if (!TextUtils.isEmpty(id)) {
                        Method findVolumeById = storageManager.getClass().getDeclaredMethod("findVolumeById", String.class);
                        Object sharedObj = findVolumeById.invoke(storageManager, id.replace("private", "emulated"));//VolumeInfo
                        readable = (boolean) isMountedReadable.invoke(sharedObj);

                        UUID volumeId = getUuid(fsUuid);
                        UUID shareVolumeId = getUuid((String) getFsUuid.invoke(sharedObj));
                        //查询内置内存中的应用，外置内存同理
                        measure(volumeId, readable ? shareVolumeId : null);
                    }

                } else if (type == 0) {//TYPE_PUBLIC
                    //外置存储
                }
            }
            Log.d(TAG, "总内存 total = " + getUnit(total, 1000) + " ,已用 used = " + getUnit(used, 1000));
            Message msg = handler.obtainMessage();
            msg.obj = "总内存 total = " + getUnit(total, 1000) + "\n已用 used = " + getUnit(used, 1000) + "\n可用 available = " + getUnit(total - used, 1000);
            msg.sendToTarget();

        } catch (SecurityException e) {
            AlertDialog ad = new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("缺少权限：permission.PACKAGE_USAGE_STATS\n" + e.getMessage() + "\n需要在\"设置>安全\"中给应用提供权限")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ToastUtils.show(StorageActivity.this, "已拒绝，请手动开启");
                        }
                    })
                    .create();
            ad.show();
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show(this, "无法获取应用内存大小");
        }
    }
}
