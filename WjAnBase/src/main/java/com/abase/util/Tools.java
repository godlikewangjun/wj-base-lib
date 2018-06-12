package com.abase.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 操作工具集合
 *
 * @author 王军
 * @date 2014年12月15日
 * @版本 1.0
 */
public class Tools {
    public static Toast toast;// 提示
    private static Handler handler = new Handler();// 控制线程
    private static boolean isShowDeBug = true;// 提示
    public static int[] wh;// 屏幕的宽和高
    public static Bitmap mBitmap = null;

    private static final int SHOWTOAST = 3000;
    private static Runnable runnable = new Runnable() {
        public void run() {
            toast.cancel();
        }
    };

    /**
     * 读取properties
     */
    public static Properties getPropertiesValue(Context context) {
        Properties properties = new Properties();
        if (context != null && !context.isRestricted()) {
            try {
                properties.load(context.getAssets().open("peizi.properties"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    /**
     * 保存配置文件
     *
     * @param context
     * @param file
     * @param properties
     * @return
     */
    public static boolean saveProperty(Context context, String file,
                                       Properties properties) {
        try {
            File fil = new File(file);
            if (!fil.exists())
                fil.createNewFile();
            FileOutputStream s = new FileOutputStream(fil);
            properties.store(s, "");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 获取签名
     */
    public static String getSign(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> iter = apps.iterator();
        while (iter.hasNext()) {
            PackageInfo packageinfo = iter.next();
            String packageName = packageinfo.packageName;
            if (packageinfo.packageName.equals(packageName)) {

            }
            return packageinfo.signatures[0].toCharsString();
        }
        return null;
    }

    /**
     * Java文件操作 获取文件扩展名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * <p>将base64字符保存文本文件</p>
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void toFile(String base64Code, String targetPath) throws Exception {
        byte[] buffer = base64Code.getBytes();
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }

    /**
     * 　　* 将base64转换成bitmap图片
     * 　　*
     * 　　* @param string base64字符串
     * 　　* @return bitmap
     *
     */
    public static Bitmap base64ToBitmap(String base64String) {
        Bitmap bitmap = null;
        byte[] bitmapArray;
        try {
            bitmapArray = Base64.decode(base64String, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 判断网络连接是否已开
     *
     * @param context
     * @return true网络打开
     */
    public static boolean getNetwStates(Context context) {
        ConnectivityManager cManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        return info.isAvailable();
    }

    /**
     * 获取资源颜色
     *
     * @param context
     * @param color
     * @return
     */
    public static int getColor(Context context, int color) {
        return context.getResources().getColor(color);
    }

    /**
     * 获取draw
     *
     * @param context
     * @return
     */
    public static Drawable getDrawable(Context context, int drawable) {
        return context.getResources().getDrawable(drawable);
    }

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    public static void showTip(final Context context, final String text) {
        handler.removeCallbacks(runnable);// 消除
        if (toast != null) {
            toast.setText(text);
        } else {
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        }
        handler.postDelayed(runnable, SHOWTOAST + 2000);
        toast.show();
    }

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    public static void showTipOne(final Context context, final String text, final int gravity) {
        handler.removeCallbacks(runnable);// 消除
        if (toast != null) {
            toast.setText(text);
        } else {
            toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(gravity, 0, 0);
        }
        handler.postDelayed(runnable, SHOWTOAST + 2000);
        toast.show();
    }

    /**
     * <p>将文件转成base64 字符串</p>
     * 文件路径
     *
     * @return
     * @throws Exception
     */
    public static String encodeBase64File(File file) throws Exception {
        return Base64.encodeToString(AbImageUtil.bitmap2Bytes(AbImageUtil.getBitmap(file), Bitmap.CompressFormat.JPEG, true), Base64.DEFAULT);
    }

    /**
     * <p>将base64字符解码保存文件</p>
     *
     * @param base64Code
     * @param targetPath
     * @throws Exception
     */
    public static void decoderBase64File(String base64Code, String targetPath) throws Exception {
        byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    public static void showToast(final Context context, final String text) {
        Toast mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        mToast.setText(text);
        mToast.show();
    }

    /**
     * 显示toast
     *
     * @param context activity
     * @param text    显示的内容
     */
    public static void showTip(final Context context, final String text,
                               final int gravity) {
        Toast mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        mToast.setText(text);
        mToast.setGravity(gravity, 0, 0);
        mToast.show();
    }

    public static void showTip(final Context context, final String text,
                               final int gravity, int x, int y) {
        Toast mToast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        mToast.setText(text);
        mToast.setGravity(gravity, x, y);
        mToast.show();
    }

    /**
     * @param source
     * @return true 表示空
     */
    public static boolean isEmpty(String source) {

        if (source == null || "".equals(source) || "null".equals(source))
            return true;

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    /**
     * 处理电话号码
     *
     * @param phonenum
     * @return
     */
    public static String hintPhoneNum(String phonenum) {
        if (phonenum == null) {
            return phonenum;
        } else if (phonenum.length() != 11) {
            return phonenum;
        }
        return phonenum.substring(0, 3) + "*****" + phonenum.substring(8);
    }


    /**
     * 字符串加密
     */
    public static String setMD5(String str) {
        if (str == null || str.equals("")) {
            return "";
        }
        try {
            MessageDigest bmd5 = MessageDigest.getInstance("MD5");
            bmd5.update(str.getBytes());
            int i;
            StringBuffer buf = new StringBuffer();
            byte[] b = bmd5.digest();
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        String versionName = "";
        int versioncode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return 0;
            }
        } catch (Exception e) {
        }
        return versioncode;
    }

    /**
     * 获取版本名称
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        int versioncode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
        }
        return versionName;
    }

    /**
     * 获取通知栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 获取view
     *
     * @param context 要获取view的content
     * @param id      获取view的资源id
     * @return 获取到的view
     */
    public static View getContentView(Context context, int id) {
        return LayoutInflater.from(context).inflate(id, null);
    }


    /**
     * 随机生成一个多少位的大小写字母加数字组成的字串
     *
     * @return 生成的字符串
     */
    public static String randompwd(int count) {
        Random r = new Random();
        int i = 0;
        String str = "";
        String s = null;
        while (i < count) { // 这个地方的30控制产生几位随机数，这里是产生30位随机数
            switch (r.nextInt(63)) {
                case (0):
                    s = "0";
                    break;
                case (1):
                    s = "1";
                    break;
                case (2):
                    s = "2";
                    break;
                case (3):
                    s = "3";
                    break;
                case (4):
                    s = "4";
                    break;
                case (5):
                    s = "5";
                    break;
                case (6):
                    s = "6";
                    break;
                case (7):
                    s = "7";
                    break;
                case (8):
                    s = "8";
                    break;
                case (9):
                    s = "9";
                    break;
                case (10):
                    s = "a";
                    break;
                case (11):
                    s = "b";
                    break;
                case (12):
                    s = "c";
                    break;
                case (13):
                    s = "d";
                    break;
                case (14):
                    s = "e";
                    break;
                case (15):
                    s = "f";
                    break;
                case (16):
                    s = "g";
                    break;
                case (17):
                    s = "h";
                    break;
                case (18):
                    s = "i";
                    break;
                case (19):
                    s = "j";
                    break;
                case (20):
                    s = "k";
                    break;
                case (21):
                    s = "m";
                    break;
                case (23):
                    s = "n";
                    break;
                case (24):
                    s = "o";
                    break;
                case (25):
                    s = "p";
                    break;
                case (26):
                    s = "q";
                    break;
                case (27):
                    s = "r";
                    break;
                case (28):
                    s = "s";
                    break;
                case (29):
                    s = "t";
                    break;
                case (30):
                    s = "u";
                    break;
                case (31):
                    s = "v";
                    break;
                case (32):
                    s = "w";
                    break;
                case (33):
                    s = "l";
                    break;
                case (34):
                    s = "x";
                    break;
                case (35):
                    s = "y";
                    break;
                case (36):
                    s = "z";
                    break;
                case (37):
                    s = "A";
                    break;
                case (38):
                    s = "B";
                    break;
                case (39):
                    s = "C";
                    break;
                case (40):
                    s = "D";
                    break;
                case (41):
                    s = "E";
                    break;
                case (42):
                    s = "F";
                    break;
                case (43):
                    s = "G";
                    break;
                case (44):
                    s = "H";
                    break;
                case (45):
                    s = "I";
                    break;
                case (46):
                    s = "L";
                    break;
                case (47):
                    s = "J";
                    break;
                case (48):
                    s = "K";
                    break;
                case (49):
                    s = "M";
                    break;
                case (50):
                    s = "N";
                    break;
                case (51):
                    s = "O";
                    break;
                case (52):
                    s = "P";
                    break;
                case (53):
                    s = "Q";
                    break;
                case (54):
                    s = "R";
                    break;
                case (55):
                    s = "S";
                    break;
                case (56):
                    s = "T";
                    break;
                case (57):
                    s = "U";
                    break;
                case (58):
                    s = "V";
                    break;
                case (59):
                    s = "W";
                    break;
                case (60):
                    s = "X";
                    break;
                case (61):
                    s = "Y";
                    break;
                case (62):
                    s = "Z";
                    break;
            }
            i++;
            str = s + str;
        }
        return str;
    }

    /**
     * 获取本机的号码信息
     *
     * @param context
     * @return 号码和运营商信息
     */
    public static Map<String, String> getSelfPhoneInfo(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        String ProvidersName = null;
        // 返回唯一的用户ID;就是这张卡的编号神马的
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        String IMSI = telephonyManager.getSubscriberId();
        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            ProvidersName = "中国移动";
        } else if (IMSI.startsWith("46001")) {
            ProvidersName = "中国联通";
        } else if (IMSI.startsWith("46003")) {
            ProvidersName = "中国电信";
        }
        // 返回
        Map<String, String> map = new HashMap<String, String>();
        map.put("phonenum", telephonyManager.getLine1Number());// //号码
        map.put("type", ProvidersName);// 运营商
        return map;
    }

    /**
     * 读取通讯录 获取 电话号码和名称
     *
     * @param context
     * @return
     */
    public static ArrayList<Map<String, String>> getPhoneAddressBook(
            Context context) {
        /** 获取库Phon表字段 **/
        final String[] PHONES_PROJECTION = new String[]{Phone.DISPLAY_NAME,
                Phone.NUMBER, Photo.PHOTO_ID, Phone.CONTACT_ID};
        /** 联系人显示名称 **/
        final int PHONES_DISPLAY_NAME_INDEX = 0;
        /** 电话号码 **/
        final int PHONES_NUMBER_INDEX = 1;
        /** 联系人信息列 **/
        ArrayList<Map<String, String>> mContacts = new ArrayList<Map<String, String>>();
        String phoneNumber;// 电话号码
        String contactName;// 名字

        ContentResolver resolver = context.getContentResolver();// 初始数据连接
        // 获取Sims卡联系人
        Uri uri = Uri.parse("content://icc/adn");
        Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
                null);
        Map<String, String> map;// 字段集

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                map = new HashMap<String, String>();
                // 得到手机号码
                phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber)) {
                    map.put("phonenum", phoneNumber);
                    continue;
                }
                // 得到联系人名称
                contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                map.put("name", contactName);
                // Sim卡中没有联系人头像
                mContacts.add(map);
            }
        }
        // 获取手机联系人
        phoneCursor = resolver.query(Phone.CONTENT_URI, PHONES_PROJECTION,
                null, null, null);
        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                map = new HashMap<String, String>();
                // 得到手机号码
                phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                // 当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber)) {
                    map.put("phonenum", phoneNumber);
                    continue;
                }
                // 得到联系人名称
                contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
                map.put("name", contactName);
            }
            phoneCursor.close(); // 关闭数据连接
        }
        return mContacts;
    }


    /**
     * 把drawable转成bitmap
     *
     * @param drawable
     * @return bitmap
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth(); // 取drawable的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565; // 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config); // 建立对应bitmap
        Canvas canvas = new Canvas(bitmap); // 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas); // 把drawable内容画到画布中
        return bitmap;
    }



    /**
     * 获取屏幕大小
     * 1是宽 2是高
     */
    public static int[] getScreenWH(Activity activity) {
        if (wh != null && wh[0] != 0 && wh[1] != 0) {
            return wh;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        int width = 0;
        int height = 0;
        width = displayMetrics.widthPixels;

        height = displayMetrics.heightPixels - getStatusBarHeight(activity);// 去掉通知栏的高度
        int[] is = {width, height};
        wh = is;
        return is;
    }

    /**
     * 获取屏幕大小
     * <p>
     * 1是宽 2是高
     */
    public static int[] getScreenWH(Context context) {
        if (wh != null && wh[0] != 0 && wh[1] != 0) {
            return wh;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        int width = 0;
        int height = 0;
        width = displayMetrics.widthPixels;

        height = displayMetrics.heightPixels - getStatusBarHeight(context);// 去掉通知栏的高度
        int[] is = {width, height};
        wh = is;
        return is;
    }

    /**
     * 打开网站服务条款
     */
    public static void openWebText(Context activity, TextView textView, View.OnClickListener onClickListener) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = textView.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) textView.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans(); // should clear old spans
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL(), activity,
                        null,onClickListener);
                style.setSpan(myURLSpan, sp.getSpanStart(url),
                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
                style.setSpan(redSpan, sp.getSpanStart(url),
                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            textView.setText(style);
        }
    }

    /**
     * 打开网站服务条款
     */
    public static void openWebText(Context activity,TextView textView,
                                   String title,View.OnClickListener onClickListener) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = textView.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) textView.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans(); // should clear old spans
            for (URLSpan url : urls) {
                MyURLSpan myURLSpan = new MyURLSpan(url.getURL(), activity,
                        title,onClickListener);
                style.setSpan(myURLSpan, sp.getSpanStart(url),
                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
                style.setSpan(redSpan, sp.getSpanStart(url),
                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            textView.setText(style);
        }
    }

    /**
     * 打开文本html链接的处理
     */
    public static void openWebText(Context activity,TextView textView,
                                   ClickableSpan mSpan) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        CharSequence text = textView.getText();
        if (text instanceof Spannable) {
            int end = text.length();
            Spannable sp = (Spannable) textView.getText();
            URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
            SpannableStringBuilder style = new SpannableStringBuilder(text);
            style.clearSpans(); // should clear old spans
            for (URLSpan url : urls) {
                style.setSpan(mSpan, sp.getSpanStart(url), sp.getSpanEnd(url),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
                style.setSpan(redSpan, sp.getSpanStart(url),
                        sp.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            textView.setText(style);
        }
    }

    /**
     * 处理textview点击超文本的方法
     *
     * @author 王军
     * @date 2014年12月26日
     * @版本 1.0
     */
    public static class MyURLSpan extends ClickableSpan {
        private String mUrl;
        private Context activity;
        private String title;// 标题
        private View.OnClickListener onClickListener;

        MyURLSpan(String url, Context activity, String title, View.OnClickListener onClickListener) {
            this.activity = activity;
            this.mUrl = url;
            this.title = title;
            this.onClickListener=onClickListener;
        }

        @Override
        public void onClick(View widget) {
            onClickListener.onClick(widget);
        }
    }

    /**
     * 修改textview的字体颜色
     *
     * @param start 开始位置
     * @param end   结束位置
     * @param color 颜色
     */
    public static void changTextViewColor(TextView textView, int start,
                                          int end, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder(textView
                .getText().toString());
        // ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
        ForegroundColorSpan redSpan = new ForegroundColorSpan(color);
        builder.setSpan(redSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(builder);
    }

    /**
     * 根据屏幕缩放
     *
     * @param activity
     * @param view     操作的view
     * @param sw       按屏幕大小缩放的宽
     * @param sh       按屏幕大小缩放的高
     */
    public static void zoomView(Activity activity, View view, int sw, int sh) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
                getScreenWH(activity)[0] / sw, getScreenWH(activity)[1] / sh));
    }

    /**
     * 判断手机号是否正确
     *
     * @param mobiles
     * @return
     */
    public static boolean isMobileNum(String mobiles) {
        Pattern p = Pattern
                .compile("^((\\+86)|(86))?1(3[0-9]|7[0-9]|8[0-9]|47|5[0-3]|5[5-9])\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 判断密码是否符合标准
     *
     * @param mobiles
     * @return
     */
    public static boolean isPwdNum(String mobiles) {
        Pattern p = Pattern
                .compile("^(?:([a-z])|([A-Z])|([0-9])|(.)){6,}|(.)+$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 判断是否全是数字
     * mobiles
     *
     * @return
     */
    public static boolean isNum(String str) {
        Pattern p = Pattern
                .compile("^[0-9]*$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 判断是否全是字母
     * mobiles
     *
     * @return
     */
    public static boolean isAbc(String str) {
        Pattern p = Pattern
                .compile("[a-zA-Z]");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 是否是email格式
     *
     * @param mobiles
     * @return
     */
    public static boolean isEmailNum(String mobiles) {
        Pattern p = Pattern
                .compile("^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 隐藏字符串
     *
     * @param content
     * @return
     */
    public static String hintStr(String content) {
        if (content.equals("") || content == null) {
            return content;
        }
        String str = null;
        if (content.length() > 3 && content.length() <= 6) {
            str = "***" + content.substring(3);
        } else if (content.length() <= 3) {
            str = "*" + content.substring(1);
        } else if (content.length() > 6 && content.length() <= 10) {
            str = "******" + content.substring(6);
        } else if (content.length() > 10) {
            str = "**********" + content.substring(9);
        }
        return str;
    }

    /**
     * 如果缓存的图片过大久删除缓存文件
     */
    public static void cleanCancle(Context context) {
        try {
//			AbLogUtil.d(context, getFolderSize(new File(AbFileUtil.getCacheDownloadDir(context)))+"当前缓存的容量");
            AbFileUtil.deleteFile(new File(AbFileUtil.getCacheDownloadDir(context)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long 单位为M
     * @throws Exception
     */
    public static long getFolderSize(File file) throws Exception {
        long size = 0;
        File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                size = size + getFolderSize(fileList[i]);
            } else {
                size = size + fileList[i].length();
            }
        }
        return size / (1024 * 1024);
    }

    /**
     * 计算出该TextView中文字的长度(像素)
     */
    public static float getTextViewLength(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        // 得到使用该paint写上text的时候,像素为多少
        float textLength = paint.measureText(text);
        return textLength;
    }

    /**
     * 获取sdk的版本号
     */
    public static int getSDKCode() {
        int osVersion;
        try {
            osVersion = Integer.valueOf(Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            osVersion = 0;
        }
        return osVersion;
    }

    /**
     * 从相册得到的url转换为SD卡中图片路径 兼容4.4
     */
    @SuppressLint("NewApi")
    public static String getPath(Uri uri, Context context) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * asset 转 string
     */
    public static String assetToString(Context context, String assetName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(assetName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }


}
