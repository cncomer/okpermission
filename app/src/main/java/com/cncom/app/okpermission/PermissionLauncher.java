package com.cncom.app.okpermission;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Arrays;
import java.util.List;

public class PermissionLauncher {

    public static interface PermissionSuccess {
        public void onGranted();
    }

    public static interface PermissionFail {
        /**
         * @param rejectPermissionList 拒绝的权限
         */
        public void onDenied(List<String> rejectPermissionList);
    }

    private static final String TAG = "PermissionLauncher";
    private FragmentActivity hostActivity;
    private Fragment hostFragment;

    private PermissionRequestFragment permissionRequestFragment = new PermissionRequestFragment();

    public PermissionLauncher with(@NonNull FragmentActivity activity) {
        this.hostActivity = activity;
        return this;
    }

    public PermissionLauncher with(@NonNull Fragment fragment) {
        this.hostFragment = fragment;
        return this;
    }

    /**
     * 当权限被授予时回调
     * @param permissionSuccessCallBack
     * @return
     */
    public PermissionLauncher granted(PermissionSuccess permissionSuccessCallBack) {
        permissionRequestFragment.permissionSuccessCallBack = permissionSuccessCallBack;
        return this;
    }

    /**
     * 当权限被被拒绝时回调
     * @param permissionFailCallBack
     * @return
     */
    public PermissionLauncher denied(PermissionFail permissionFailCallBack) {
        permissionRequestFragment.permissionFailCallBack = permissionFailCallBack;
        return this;
    }

    Context getContext() {
        return hostActivity != null ? hostActivity : hostFragment.getContext();
    }
    Activity getActivity() {
        return hostActivity != null ? hostActivity : hostFragment.getActivity();
    }

    public void request(@NonNull String permission) {
        request(new String[]{permission});
    }
    public void request(@NonNull String permission, @Nullable String permissionsName, @Nullable String permissionsRationale) {
        request(new String[]{permission}, new String[]{permissionsName}, new String[]{permissionsRationale});
    }

    /**
     * 请求权限
     * @param permissions 权限列表
     */
    public void request(@NonNull String[] permissions) {
        request(permissions, null, null);
    }

    public void request(@NonNull String[] permissions, @Nullable String[] permissionsName, @Nullable String[] permissionsRationale) {
        doRequest(permissions, permissionsName, permissionsRationale);
    }

    private void doRequest(String[] permissions, @Nullable String[] permissionsName, @Nullable String[] permissionsRationale) {
        permissionRequestFragment.setPermissions(permissions, permissionsName, permissionsRationale);
        if (permissionRequestFragment.checkPermission(getContext())) {
            //如果所有权限都已经授权了，直接回调已授权
            permissionRequestFragment.permissionSuccessCallBack.onGranted();
            return;
        }

        int needPermissionRationaleCount = 0;
        for(String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                needPermissionRationaleCount++;
            }
        }
        if (needPermissionRationaleCount == permissions.length) {
            //所有权限都需要给出理由，所以我们直接拒绝
            logD(TAG, "request shouldShowRequestPermissionRationale onDenied");
            permissionRequestFragment.permissionFailCallBack.onDenied(Arrays.asList(permissions));
            return;
        }

        if (hostActivity != null) {
            permissionRequestFragment.fragmentManager = hostActivity.getSupportFragmentManager();
            logD(TAG, "request from activity");

        } else if (hostFragment != null) {
            logD(TAG, "request from fragment");
            permissionRequestFragment.fragmentManager = hostFragment.getChildFragmentManager();

        }
        permissionRequestFragment.fragmentManager.beginTransaction().add(permissionRequestFragment, "PermissionRequestFragment").commitAllowingStateLoss();
    }

    /**
     * 打开APP详情页
     * @param context
     * @throws ActivityNotFoundException
     */
    public static void openAppDetailSettings(Context context) throws ActivityNotFoundException{
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(intent);
    }

    /**
     * 打开定位服务配置界面
     * @param context
     */
    public static void openLocationSourceSettings(Context context) throws ActivityNotFoundException {
        Intent intent = getLocationSourceSettingsIntent();
        if (context instanceof Activity) {

        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public static Intent getLocationSourceSettingsIntent() {
        return new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    private static void logD(String tag, String MSG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, MSG==null?"":MSG);
        }
    }
}