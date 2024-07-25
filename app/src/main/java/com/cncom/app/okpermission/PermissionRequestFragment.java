package com.cncom.app.okpermission;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionRequestFragment extends Fragment {
    PermissionLauncher.PermissionSuccess permissionSuccessCallBack;
    PermissionLauncher.PermissionFail permissionFailCallBack;
    private ActivityResultLauncher<String[]> activityResultLauncher;
    private String[] permissions = null;
    private boolean hasCalled = false;
    FragmentManager fragmentManager;

    private String[] permissionsName, permissionsRationale;
    private WindowManager windowManager;
    private View rationalView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::responsePermissionResult);
    }

    protected View createRationalView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rationalView = inflater.inflate(R.layout.request_permission_rationale, null, false);
        ViewGroup rational_root = rationalView.findViewById(R.id.rational_root);
        rational_root.removeAllViews();
        TextView permissionNameTextView = null;
        TextView permissionRationalTextView  = null;
        int rationaleCount = permissionsName != null && permissionsName.length >= 1 ? permissionsName.length : 0;
        for(int index = 0; index < rationaleCount; index++) {
            View item = inflater.inflate(R.layout.request_permission_rationale_item, rational_root, false);
            permissionNameTextView = item.findViewById(R.id.permission_name);
            permissionRationalTextView = item.findViewById(R.id.permission_rational);
            permissionNameTextView.setText(permissionsName[index]);
            permissionRationalTextView.setText(permissionsRationale[index]);
            rational_root.addView(item);
        }
        return rationalView;
    }

    void setPermissions(@NonNull String[] permissions, @Nullable String[] permissionsName, @Nullable String[] permissionsRationale) {
        if (permissionsName != null
                && permissionsRationale != null
                && permissionsName.length > 0
                && permissionsRationale.length > 0
                && permissionsName.length == permissionsRationale.length) {

            this.permissions = permissions;
            this.permissionsName = permissionsName;
            this.permissionsRationale = permissionsRationale;
            hasCalled = false;
        } else if (permissionsName == null && permissionsRationale == null) {
            this.permissions = permissions;
            hasCalled = false;
        } else {
            throw new IllegalArgumentException("permissionsName and permissionsRational must have same length");
        }
    }

    /**
     * 检查权限
     * @return true表示已经具备所需权限
     */
    boolean checkPermission(Context context) {
        for(String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasCalled) {
            hasCalled = true;
            rationalView = hasRational() ? createRationalView() : null;
            if (rationalView != null) {
//                Rect frame = new Rect();
//                getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                windowManager = getActivity().getWindowManager();
//                int statusBarHeight = frame.top;
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.width = displayMetrics.widthPixels - ((int)(32 * displayMetrics.density));
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//                lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
                lp.gravity = Gravity.TOP | Gravity.CENTER_VERTICAL;
//                lp.y = statusBarHeight;
                windowManager.addView(rationalView, lp);
            }

            activityResultLauncher.launch(permissions);
        }
    }

    boolean hasRational() {
        return permissionsName != null && permissionsName.length >= 1;
    }

    private void responsePermissionResult(Map<String, Boolean> result) {
        if (rationalView != null) {
            if (windowManager != null) windowManager.removeView(rationalView);
            rationalView = null;
        }
        Set<String> permissions = result.keySet();
        List<String> rejectPermissionList = new ArrayList<>(permissions.size());
        for (String permission : permissions) {
            if (Boolean.FALSE.equals(result.get(permission))) {
                rejectPermissionList.add(permission);
            }
        }
        if (rejectPermissionList.isEmpty()) {
            permissionSuccessCallBack.onGranted();
        } else {
            permissionFailCallBack.onDenied(rejectPermissionList);
        }
        //授权完毕要移除请求授权页面
        fragmentManager.beginTransaction().remove(this).commitAllowingStateLoss();
    }
}