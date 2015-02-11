package com.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by chenjianjun on 14-11-20.
 * <p/>
 * Beyond their own, let the future
 */
public class PreferenceUtils {

    /**
     *
     */
    public enum DataType {
        STRING,
        INT,
        LONG,
        FLOAT
    }

    /**
     * 存储程序信息
     */
    public static final String PREFERENCE_NAME = "ep";// 存储文件名

    // 用户名
    public static final String PREFERENCE_USER_NAME = "userName";
    // 密码
    public static final String PREFERENCE_PASS_WORD = "passWord";
    // 通道ID
    public static final String PREFERENCE_CHANNEL_ID = "channelID";
    // APP ID
    public static final String PREFERENCE_APP_ID = "appID";
    // 用户ID
    public static final String PREFERENCE_USER_ID = "userID";


    public static <T> void saveValue(Context context, String key, DataType type, T value) {
        if (TextUtils.isEmpty(key)) {
            return;
        }

        SharedPreferences.Editor edit = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE).edit();

        switch (type) {
            case STRING: {
                edit.putString(key, (String) value);
                break;
            }
            case INT: {
                edit.putInt(key, (Integer) value);
                break;
            }
            case LONG: {
                edit.putLong(key, (Long) value);
                break;
            }
            case FLOAT: {
                edit.putFloat(key, (Float) value);
                break;
            }
            default:
                break;
        }

        edit.commit();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Object> T getValue(Context context, String key, DataType valueType) {

        switch (valueType) {
            case STRING: {
                return (T) ((Object) context.getSharedPreferences(PREFERENCE_NAME,
                        Context.MODE_PRIVATE).getString(key, null));
            }
            case INT: {
                return (T) ((Object) context.getSharedPreferences(PREFERENCE_NAME,
                        Context.MODE_PRIVATE).getInt(key, -1));
            }
            case LONG: {
                return (T) ((Object) context.getSharedPreferences(PREFERENCE_NAME,
                        Context.MODE_PRIVATE).getLong(key, -1));
            }
            case FLOAT: {
                return (T) ((Object) context.getSharedPreferences(PREFERENCE_NAME,
                        Context.MODE_PRIVATE).getFloat(key, -1));
            }
            default:
                break;
        }

        return (T) (Object) (null);
    }

    public static boolean hasBind(Context context) {

        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        String flag = sp.getString("bind_flag", "");
        if ("ok".equalsIgnoreCase(flag)) {
            return true;
        }

        return false;
    }

    public static void setBind(Context context, boolean flag) {

        String flagStr = "not";
        if (flag) {
            flagStr = "ok";
        }

        SharedPreferences sp = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("bind_flag", flagStr);
        editor.commit();

    }

    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查目录是否存在， 如果不存在则创建
     *
     * @param dirs
     */
    public static void checkDirsExists(String... dirs) {
        for (int i = 0; i < dirs.length; i++) {
            File dir = new File(dirs[i]);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    public static void delDirsExists(String... dirs) {
        for (int i = 0; i < dirs.length; i++) {
            File dir = new File(dirs[i]);
            if (dir.exists()) {
                dir.delete();
            }
        }
    }
}

