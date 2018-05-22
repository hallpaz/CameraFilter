package br.edu.infnet.camerafilter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import static android.graphics.Color.rgb;

public class MainActivity extends AppCompatActivity {

    private final int CODE_FOR_IMAGE_CAPTURE = 51;

    private ImageView photoImageView;
    private Bitmap originalBitmap;
    private int width = 0;
    private int height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoImageView = findViewById(R.id.photo);
    }

    public void takePhoto(View v){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CODE_FOR_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_FOR_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            originalBitmap = (Bitmap) extras.get("data");
            width = originalBitmap.getWidth();
            height = originalBitmap.getHeight();
            photoImageView.setImageBitmap(originalBitmap);
        }
    }

    protected int[] computeIntensity(int[] pixels, int width, int height) {
        int[] intensity = new int[pixels.length];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int color = pixels[i*width + j];
                int A = (color >> 24) & 0xff;
                int R = (color >> 16) & 0xff;
                int G = (color >>  8) & 0xff;
                int B = (color      ) & 0xff;
                R = (int) (R * 0.299);
                G = (int) (G * 0.587);
                B = (int) (B * 0.114);
                int lum = R+G+B;

                intensity[i*width + j] = (A & 0xff) << 24 | (lum & 0xff) << 16 | (lum & 0xff) << 8 | (lum & 0xff);
            }
        }
        return intensity;
    }

    public Bitmap applyGrayScale(Bitmap bitmap){
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, bitmap.getConfig());
        int[] pixels = new int[width*height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] black = computeIntensity(pixels, width, height);
        resultBitmap.setPixels(black, 0, width, 0, 0, width, height);
        return resultBitmap;
        //matrix[ i ][ j ] = array[ i*m + j ]
    }

    public Bitmap applyInverse(Bitmap bitmap){
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, bitmap.getConfig());
        for (int i=0; i<width; i++){
            for (int j=0; j<height; j++){
                resultBitmap.setPixel(i, j, bitmap.getPixel(width-i-1, j));
            }
        }
        return resultBitmap;
    }

    public Bitmap applyEdgeFilter(Bitmap bitmap){
        return null;
    }

    public void applyFilter(View v){
        Button btn = (Button) v;
        Bitmap resultBitmap = originalBitmap;
        switch (btn.getId()){
            case R.id.grayscale:
                resultBitmap = applyGrayScale(originalBitmap);
                break;
            case R.id.invert:
                resultBitmap = applyInverse(originalBitmap);
                break;
            case R.id.edge:
                resultBitmap = applyEdgeFilter(originalBitmap);
                break;
            default:
                Log.d("XABU", "Botão inválido!!!");
        }
        photoImageView.setImageBitmap(resultBitmap);
    }
}
