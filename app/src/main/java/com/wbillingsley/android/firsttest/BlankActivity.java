package com.wbillingsley.android.firsttest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Random;

public class BlankActivity extends ActionBarActivity {

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        surfaceView = (SurfaceView) findViewById(R.id.surf);
        surfaceHolder = surfaceView.getHolder();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    boolean isRecording = false;
    final int mb = 4096;
    final int sampleRate = 44100;

    final int ms100 = 4096;
    final FFT fft100 = new FFT(ms100);
    final int ms10 = 512;
    final FFT fft10 = new FFT(ms10);

    FFT fft = fft100;

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Random random = new Random();

    public void record(View view) {


        //final int mb = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mb);
        recorder.startRecording();
        isRecording = !isRecording;
        Thread recordingThread = new Thread(new Runnable() {
            public void run() {

                short[] miniBuf = new short[mb];
                double[] doubleBuf = new double[mb];

                double[] magBuf = new double[mb];


                while (isRecording) {
                    recorder.read(miniBuf, 0, mb);


                    for (int i = 0 ; i < mb; i++) {
                        doubleBuf[i] = miniBuf[i];
                    }
                    double[] zeros = new double[mb];

                    fft.fft(doubleBuf, zeros);

                    double max = 0;
                    int maxIdx = 0;
                    for (int i = 0; i < mb / 2; i++) {
                        magBuf[i] = Math.sqrt(Math.pow(doubleBuf[i], 2) + Math.pow(zeros[i], 2));
                        if (magBuf[i] > max) {
                            max = magBuf[i];
                            maxIdx = i;
                        }
                    }

                    //System.out.println("Max is " + (maxIdx * 1.0 * sampleRate / mb) + "Hz with val " + max);

                    if(surfaceHolder.getSurface().isValid()){
                        Canvas canvas = surfaceHolder.lockCanvas();

                        int w = canvas.getWidth();
                        int h = canvas.getHeight();

                        paint.setColor(Color.BLUE);
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawRect(0, 0, w, h, paint);
                        //... actual drawing on canvas

                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(3);
                        paint.setColor(0xffa0a0a0);

                        final int maxHz = 3400;
                        final int maxBin = (maxHz * mb) / sampleRate;

                        for (int i = 0; i < maxBin; i++) {

                            int x = (int)(w * (1.0 * i / maxBin));
                            int y = (int)((magBuf[i] / max) * h);

                            canvas.drawLine(x, h, x, h - y, paint);

                        }

                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }


                }
                recorder.stop();

            }
        }, "AudioRecorder Thread");
        if (isRecording) {
            recordingThread.start();
        }
        System.out.println("bing!");
    }
}
