package example.admin.com.modiscript_classifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
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
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class PaintPad extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    private static final int PIXEL_WIDTH = 32;
    private Classifier mnistClassifier;


    @BindView(R.id.button_detect)
    View detectButton;

    @BindView(R.id.button_clear)
    View clearButton;

    @BindView(R.id.text_result)
    TextView mResultText;

    @BindView(R.id.paintView)
    PaintView paintView;

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
        setContentView(R.layout.activity_paint);
        setTitle(R.string.app_name);
        ButterKnife.bind(this);
        //OpenCVLoader.initDebug();
        mnistClassifier = new Classifier(this);
        detectButton = (Button) findViewById(R.id.button_detect);
        clearButton = (Button) findViewById(R.id.button_clear);
        mResultText = (TextView) findViewById(R.id.text_result);
        paintView = (PaintView) findViewById(R.id.paintView);
        inferencePreview = (LinearLayout) findViewById(R.id.inference_preview);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDetectClicked();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClicked();
            }
        });
    }
    String devchar[] = {"अ","आ","इ","उ","ए","ऐ","ओ","औ","अं","अः","क", "ख","ग","घ","ङ","च","छ","ज","झ", "ञ","ट","ठ","ड","ढ","ण","त","थ","द","ध","न","प","फ","ब","भ","म","य","र","ल","व","श","ष","स","ह","ळ","क्ष","ज्ञ","श्र","०","१","२","३","४","५","६","७","८","९"};
    private Mat get_square(Mat im,int square_size){
        //height=imagwidth=image.shap
        int differ=0;
        Bitmap b;
        b=Bitmap.createBitmap(im.width(), im.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(im, b);
        if(im.height()>im.width())
            differ=im.height();
        else
            differ=im.width();
        differ+=4;
        Size s = new Size(differ,differ);
        Mat mask=Mat.zeros(s,CvType.CV_8U);
        //mask.setTo(Scalar.all(255));
        Bitmap b1;
        b1=Bitmap.createBitmap(mask.width(), mask.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(mask, b1);
        int x_pos=(int)((differ-im.width())/2);
        int y_pos=(int)((differ-im.height())/2);
        Size sz = new Size(square_size,square_size);
        im.submat(0,im.height(),0,im.width()).copyTo(mask.submat(y_pos,y_pos+im.height(),x_pos,x_pos+im.width()));
        Bitmap b4;
        b4=Bitmap.createBitmap(im.width(), im.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(im, b4);
        Bitmap b2;
        b2=Bitmap.createBitmap(mask.width(), mask.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(mask, b2);
        Imgproc.resize(mask,mask,sz,Imgproc.INTER_AREA);
        Bitmap b3;
        b3=Bitmap.createBitmap(mask.width(), mask.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(mask, b3);
        return mask;
    }
    private void onDetectClicked() {
        inferencePreview.setVisibility(View.VISIBLE);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat img=new Mat();Mat img1=new Mat();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(paintView.getBitmap(), PIXEL_WIDTH, PIXEL_WIDTH, false);
        Bitmap bmp;
        bitmapToMat(scaledBitmap,img);
        Imgproc.cvtColor(img, img1, Imgproc.COLOR_RGB2GRAY);Mat ds = new Mat();
        bitwise_not ( img1, ds );
        Mat dst = new Mat();
        Imgproc.threshold(ds, dst, 50, 255, Imgproc.THRESH_BINARY);
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
        Rect r = Imgproc.boundingRect(contours.get(maxValIdx));
        Mat ROI = dst.submat(r.y, r.y+ r.height, r.x, r.x+ r.width);
        Mat resizeimage = new Mat();
        Size sz = new Size(28,28);
        Imgproc.resize( ROI, resizeimage, sz ,Imgproc.INTER_AREA);
        resizeimage=get_square(resizeimage,32);
        bmp = Bitmap.createBitmap(resizeimage.width(), resizeimage.height(), Bitmap.Config.ARGB_8888);
        matToBitmap(resizeimage, bmp);Log.d(TAG, "Copied = "+bmp.getPixel(24,24));
        saveToFile(getCacheFilename(),scaledBitmap);
        int digit = mnistClassifier.classify(bmp);
        if (digit >= 0) {
            Log.d(TAG, "Class = " + digit);
            mResultText.setText("Class : "+String.valueOf(digit));
            mapped.setText("Devnagri Character : "+devchar[digit-1]);
        } else {
            mResultText.setText(getString(R.string.not_detected));
        }
    }

    private void onClearClicked() {
        mResultText.setText("");
        mapped.setText("");
        paintView.clear();
    }

    public File getSavePath() {
        File path;
        if (hasSDCard()) { // SD card
            path = new File(getSDCardPath() + "/Claasification/");
            path.mkdir();
        } else {
            path = Environment.getDataDirectory();
        }
        return path;
    }
    public  String getCacheFilename() {
        File f = getSavePath();
        return f.getAbsolutePath() + "/img.png";
    }

    public static void saveToFile(String filename,Bitmap bmp) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch(Exception e) {}
    }

    public static boolean hasSDCard() { // SD????????
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }
    public static String getSDCardPath() {
        File path = Environment.getExternalStorageDirectory();
        return path.getAbsolutePath();
    }

}

