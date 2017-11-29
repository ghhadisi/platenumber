package com.sstsj.platenumber;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    private int index = 0;
    private int[] ids = {R.drawable.test1, R.drawable.test2, R.drawable.test3, R.drawable.test4,
            R.drawable.test5,R.drawable.test6,R.drawable.test7,R.drawable.test8,R.drawable.test9,R.drawable.test10};

    ImageView src,plate;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        src = (ImageView) findViewById(R.id.src);
        plate = (ImageView) findViewById(R.id.plate);
         result = (TextView) findViewById(R.id.result);

        src.setImageResource(ids[index]);
        loadAsserts();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private ProgressDialog progressDialog;
    private void showProgress() {
        if (null != progressDialog) {
            progressDialog.show();
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("请稍候...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }

    List<String> upoloadImgs;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_HEAD_IMAGE){
            if(resultCode == RESULT_OK){
                if (upoloadImgs != null){
                    upoloadImgs.clear();
                    upoloadImgs = null;
                }
                upoloadImgs = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                if (upoloadImgs !=null &&upoloadImgs.size()>0){
                    String imagePath  = upoloadImgs.get(0);
                    if (!TextUtils.isEmpty(imagePath)) {
//                        if (fullImage != null) {
//                            fullImage.recycle();
//                        }
//                        int degree = BmpUtil.readPictureDegree(imagePath);
//                        fullImage = toBitmap(imagePath);
//                        Log.e("hss","degree =  "+degree);
//                        if (degree !=0){
//                            fullImage= BmpUtil.rotateBitmap(fullImage,-degree);
//                        }
//                        tv_result.setText(null);
//                        imageView.setImageBitmap(fullImage);
                    }
                }

            }
        } else  {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }


    void loadAsserts(){
        getPermission().request(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {//AssessQuestionFragment
                        if (aBoolean){
                            try {
                                File dir = new File(Environment.getExternalStorageDirectory(), "car");
                                if (!dir.exists()){
                                    dir.mkdirs();
                                }
                                //训练代码没放在android工程中 有点乱
//            准备训练SVM车牌识别分类模型...
//            准备训练数据耗时: 4.70335
//            训练完成 耗时: 2493.37188 ,模型保存: /Users/xiang/Documents/xcodeWorkSpace/CarPlateRecognize/CarPlateRecognize/resource/HOG_SVM_DATA.xml
//            准备训练ann中文识别模型...
//            准备训练数据耗时: 2.04729
//            训练ann中文识别模型 耗时: 253.23836 ,保存: /Users/xiang/Documents/xcodeWorkSpace/CarPlateRecognize/CarPlateRecognize/resource/HOG_ANN_ZH_DATA.xml
//            准备训练ann字符识别模型...
//            准备训练数据耗时: 8.80205
//            训练ann字符识别模型耗时: 63.18750 ,保存: /Users/xiang/Documents/xcodeWorkSpace/CarPlateRecognize/CarPlateRecognize/resource/HOG_ANN_DATA.xml
                                String ann = copyAssetsFile("HOG_ANN_DATA.xml", dir);
                                String ann_zh = copyAssetsFile("HOG_ANN_ZH_DATA.xml", dir);
                                String svm = copyAssetsFile("HOG_SVM_DATA.xml", dir);
                                init(svm, ann, ann_zh);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else {
                            showCustomToast("请打开读写sd卡和调用相机的权限");
                        }

                    }
                });
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void init(String svm, String ann, String ann_zh);

    public native void release();

    public native String recognition(Bitmap bitmap, Bitmap out);


    private String copyAssetsFile(String name, File dir) throws IOException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, name);
        if (!file.exists()) {
            InputStream is = getAssets().open(name);
            FileOutputStream fos = new FileOutputStream(file);
            int len;
            byte[] buffer = new byte[2048];
            while ((len = is.read(buffer)) != -1)
                fos.write(buffer, 0, len);
            fos.close();
            is.close();
        }
        return file.getAbsolutePath();
    }
    void requestImages(){
        getPermission().request(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {//AssessQuestionFragment
                        if (aBoolean){
                            Intent intent = new Intent(MainActivity.this, MultiImageSelectorActivity.class);
                            intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
                            intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
                            startActivityForResult(intent, MainActivity.REQUEST_HEAD_IMAGE);
                        }else {
                            showCustomToast("请打开读写sd卡和调用相机的权限");
                        }

                    }
                });

    }
    RxPermissions rxPermissions ; // where this is an Activity instance
    public RxPermissions getPermission() {
        if (rxPermissions == null){
            rxPermissions = new RxPermissions(this); // where this is an Activity instance
        }
        return rxPermissions;
    }
    void showCustomToast(String str){
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();
    }

    public static final int REQUEST_HEAD_IMAGE = 2000;



    void discriminate(View v){
        BitmapFactory.Options bfoOptions = new BitmapFactory.Options();
        bfoOptions.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ids[index],bfoOptions);

        Bitmap plateBmp = Bitmap.createBitmap(136, 36, Bitmap.Config.ARGB_8888);
        String recognition = recognition(bitmap, plateBmp);
        plate.setImageBitmap(plateBmp);
        result.setText(recognition);
        bitmap.recycle();
    }

    void previous(View v){
        if (index == 0){
            return;
        }
        src.setImageResource(ids[--index]);
    }

    void next(View v){
        if (index == ids.length-1){
            return;
        }
        src.setImageResource(ids[++index]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
        dismissProgress();
    }
}
