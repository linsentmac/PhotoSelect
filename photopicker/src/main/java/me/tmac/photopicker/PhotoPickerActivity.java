package me.tmac.photopicker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.tmac.photopicker.entity.Photo;
import me.tmac.photopicker.event.OnItemCheckListener;
import me.tmac.photopicker.fragment.ImagePagerFragment;
import me.tmac.photopicker.fragment.PhotoPickerFragment;

import static android.widget.Toast.LENGTH_LONG;
import static me.tmac.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static me.tmac.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static me.tmac.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static me.tmac.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static me.tmac.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static me.tmac.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.tmac.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static me.tmac.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.tmac.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class PhotoPickerActivity extends AppCompatActivity implements View.OnClickListener{

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    private MenuItem menuDoneItem;

    private ImageView iv_back;
    private Button btn_ensure;

    private int maxCount = DEFAULT_MAX_COUNT;
    private static final int CODE_FOR_WRITE_PERMISSION = 0;

    /**
     * to prevent multiple calls to inflate menu
     */
    private boolean menuIsInflated = false;

    private boolean showGif = false;
    private int columnNumber = DEFAULT_COLUMN_NUMBER;
    private ArrayList<String> originalPhotos = null;
    private boolean showCamera = false;
    private boolean previewEnabled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);

        setShowGif(showGif);

        setContentView(R.layout.__picker_activity_photo_picker);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        btn_ensure = (Button) findViewById(R.id.get_ensure);
        iv_back.setOnClickListener(this);
        btn_ensure.setOnClickListener(this);

        /*Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(R.string.__picker_title);

        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setCustomView(R.layout.__picker_title_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }*/

        maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);

        if(Build.VERSION.SDK_INT >= 23){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                Log.d("LC-PhotoPickerActivity", "checkPermission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA
                                        , Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, CODE_FOR_WRITE_PERMISSION);
            }else {
                initFragment();
            }
        }else {
            initFragment();
        }


    }

    private void initFragment() {
        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment
                    .newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
        }

        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {
            @Override
            public boolean OnItemCheck(int position, Photo photo, final boolean isCheck, int selectedItemCount) {

                int total = selectedItemCount + (isCheck ? -1 : 1);

                //menuDoneItem.setEnabled(total > 0);

                if (maxCount <= 1) {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (total > maxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, maxCount),
                            LENGTH_LONG).show();
                    return false;
                }
                //menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, total, maxCount));
                return true;
            }
        });
    }


    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            imagePagerFragment.runExitAnimation(new Runnable() {
                public void run() {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }
            });
        } else {
            super.onBackPressed();
        }
    }


    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!menuIsInflated) {
            getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.done);
            if (originalPhotos != null && originalPhotos.size() > 0) {
                menuDoneItem.setEnabled(true);
                menuDoneItem.setTitle(
                        getString(R.string.__picker_done_with_count, originalPhotos.size(), maxCount));
            } else {
                menuDoneItem.setEnabled(false);
            }
            menuIsInflated = true;
            return true;
        }
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        // 点击完成将选中的图片集合返回
        if (item.getItemId() == R.id.done) {
            Intent intent = new Intent();
            ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return showGif;
    }

    public void setShowGif(boolean showGif) {
        this.showGif = showGif;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.iv_back){
            finish();
        }else if(v.getId() == R.id.get_ensure){
            Intent intent = new Intent();
            ArrayList<String> selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    public interface OnCaptureImgListener {
        void OnCapture(String path);
    }

    /**
     * Android 6.0 权限申请回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CODE_FOR_WRITE_PERMISSION){
            if(permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // 权限申请成功
                initFragment();
            }else {
                finish();
            }
        }
    }

}
