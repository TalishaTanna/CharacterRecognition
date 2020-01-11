package example.admin.com.modiscript_classifier;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import butterknife.BindView;

public class Choices extends AppCompatActivity {
    @BindView(R.id.button1)
    View uploadButton;

    @BindView(R.id.button2)
    View paintButton;

    @BindView(R.id.button3)
    View cameraButton;
    private static int RESULT_LOAD_IMAGE = 1;
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    Uri imageUri                      = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        uploadButton = (Button) findViewById(R.id.button1);
        paintButton = (Button) findViewById(R.id.button2);
        cameraButton = (Button) findViewById(R.id.button3);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUploadClicked();
            }
        });
        paintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPaintClicked();
            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCameraClicked();
            }
        });
    }

    private void onUploadClicked() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }
    private void onPaintClicked() {
        Intent intent = new Intent(this, PaintPad.class);
        startActivity(intent);
    }
    private void onCameraClicked() {
        Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        startActivityForResult( intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Intent intent = new Intent(this, Uploaded.class);
            intent.putExtra("GImage",selectedImage.toString());
            startActivity(intent);

        }
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Intent intent = new Intent(this, Captured.class);
            intent.putExtra("GImage",selectedImage.toString());
            startActivity(intent);
        }

    }
}
