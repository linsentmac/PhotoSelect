package me.tmac.photopicker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PhotoCamDisplayActivity extends AppCompatActivity {

    private Button btn_ensure;
    private ImageView iv_display;
    private ImageView iv_back;
    public static final int DISPLAY_PHOTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_cam_display);
        btn_ensure = (Button) findViewById(R.id.ensure);
        iv_display = (ImageView) findViewById(R.id.display_iv);
        iv_back = (ImageView) findViewById(R.id.back_iv);

        Intent intent = getIntent();
        final String path = intent.getStringExtra("display_path");
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        iv_display.setImageBitmap(bitmap);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_ensure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("callback_path", path);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }
}
