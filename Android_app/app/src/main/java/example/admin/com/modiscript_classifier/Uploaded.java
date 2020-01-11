package example.admin.com.modiscript_classifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import example.admin.com.modiscript_classifier.Classifier;
import example.admin.com.modiscript_classifier.PaintView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static org.opencv.android.Utils.bitmapToMat;
import static org.opencv.android.Utils.matToBitmap;
import static org.opencv.core.Core.bitwise_not;
import static org.opencv.core.CvType.CV_8UC3;

public class Uploaded extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private Classifier mnistClassifier;


    @BindView(R.id.button_detect)
    View detectButton;

    @BindView(R.id.text_result)
    TextView mResultText;

    @BindView(R.id.imageView)
    ImageView imageView;

    @BindView(R.id.inference_preview)
    LinearLayout inferencePreview;

    @BindView(R.id.Mapped)
    TextView mapped;
    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        setTitle(R.string.app_name);
        ButterKnife.bind(this);
        //OpenCVLoader.initDebug();
        mnistClassifier = new Classifier(this);
        detectButton = (Button) findViewById(R.id.button_detect);
        mResultText = (TextView) findViewById(R.id.text_result);
        imageView = (ImageView) findViewById(R.id.imageView);
        inferencePreview = (LinearLayout) findViewById(R.id.inference_preview);
        String image_path= getIntent().getStringExtra("GImage");
        Uri fileUri = Uri.parse(image_path);
        imageView.setImageURI(fileUri);
        Bitmap bp = null;
        try
        {
            bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);;
        }
        catch (Exception e)
        {
            //handle exception
        }
        saveToFile("readimg.png",bp);
        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDetectClicked();
            }
        });

    }
    String devchar[] = {"अ","आ","इ","उ","ए","ऐ","ओ","औ","अं","अः","क", "ख","ग","घ","ङ","च","छ","ज","झ", "ञ","ट","ठ","ड","ढ","ण","त","थ","द","ध","न","प","फ","ब","भ","म","य","र","ल","व","श","ष","स","ह","ळ","क्ष","ज्ञ","श्र","०","१","२","३","४","५","६","७","८","९"};
    private Mat get_square(Mat im,int square_size){
        int differ=0;
        if(im.height()>im.width())
            differ=im.height();
        else
            differ=im.width();
        differ+=4;
        Size s = new Size(differ,differ);
        Mat mask=Mat.zeros(s,CvType.CV_8U);
        int x_pos=(int)((differ-im.width())/2);
        int y_pos=(int)((differ-im.height())/2);
        Size sz = new Size(square_size,square_size);
        im.submat(0,im.height(),0,im.width()).copyTo(mask.submat(y_pos,y_pos+im.height(),x_pos,x_pos+im.width()));
        Imgproc.resize(mask,mask,sz,Imgproc.INTER_AREA);
        return mask;
    }
    private void onDetectClicked() {
        inferencePreview.setVisibility(View.VISIBLE);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat img=new Mat();Mat img1=new Mat();
        img= Imgcodecs.imread("//data//data//example.admin.com.modiscript_classifier//files//readimg.png",CvType.CV_8UC1);
        Bitmap scaledBitmap=null;
        scaledBitmap=Bitmap.createBitmap(img.width(), img.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(img, scaledBitmap);
        Bitmap bmp;
        bitmapToMat(scaledBitmap,img);
        Imgproc.cvtColor(img, img1, Imgproc.COLOR_BGR2GRAY);
        Mat dst = new Mat();
        Imgproc.threshold(img1 ,dst, 0, 255, Imgproc.THRESH_OTSU);
        bitwise_not ( dst, dst );
        Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
        {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea)
            {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }
        Log.d(TAG, maxValIdx+"");
        Rect r = Imgproc.boundingRect(contours.get(maxValIdx));
        Mat ROI = dst.submat(r.y, r.y+ r.height, r.x, r.x+ r.width);
        Mat resizeimage = new Mat();
        Size sz = new Size(28,28);
        Imgproc.resize( ROI, resizeimage, sz ,Imgproc.INTER_AREA);
        resizeimage=get_square(resizeimage,32);
        bmp = Bitmap.createBitmap(resizeimage.width(), resizeimage.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(resizeimage, bmp);
        int digit = mnistClassifier.classify(bmp);
        if (digit >= 0) {
            Log.d(TAG, "Class = " + digit);
            mResultText.setText("Class : "+String.valueOf(digit));
            mapped.setText("Devnagri Character : "+devchar[digit-1]);
        } else {
            mResultText.setText(getString(R.string.not_detected));
        }
    }

    public  void saveToFile(String filename,Bitmap bmp) {
        FileOutputStream out;
        File file=null;
            try {
                file=getFilesDir();
               out = openFileOutput(filename, Context.MODE_PRIVATE);
               bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
               out.flush();
               out.close();
            }
            catch (Exception e)
            {Log.d(TAG, ""+e);}
    }
}


