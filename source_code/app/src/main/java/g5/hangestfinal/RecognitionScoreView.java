/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package g5.hangestfinal;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import g5.hangestfinal.Classifier.Recognition;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class RecognitionScoreView extends View implements ResultsView {
    private static final float TEXT_SIZE_DIP = 24;
    private List<Recognition> results;
    private final float textSizePx;
    private final Paint fgPaint;
    private final Paint bgPaint;
    Bitmap bitmaps[];


    public RecognitionScoreView(final Context context, final AttributeSet set) {
        super(context, set);
        bitmaps = new Bitmap[6];
        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        fgPaint = new Paint();
        fgPaint.setTextSize(textSizePx);
        fgPaint.setColor(Color.parseColor("#FFFFFF"));

        bgPaint = new Paint();
        bgPaint.setColor(Color.parseColor("#00000000"));
        Resources res = getResources();
        bitmaps[0] = BitmapFactory.decodeResource(res, R.drawable.five);
        bitmaps[1] = BitmapFactory.decodeResource(res, R.drawable.fist);
        bitmaps[2] = BitmapFactory.decodeResource(res, R.drawable.peace);
        bitmaps[3] = BitmapFactory.decodeResource(res, R.drawable.palm);
        bitmaps[4] = BitmapFactory.decodeResource(res, R.drawable.c_shape);
        bitmaps[5] = BitmapFactory.decodeResource(res, R.drawable.point);
    }

    @Override
    public void setResults(final List<Recognition> results) {
        this.results = results;
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {
        final int x = 250;
        int y = (int) (fgPaint.getTextSize() * 1.5f);

        canvas.drawPaint(bgPaint);
        DecimalFormat Format = new DecimalFormat("#0.00");
        if (results != null) {


            try {
                if (results.get(0).getConfidence() > 0.00) {

                    switch (results.get(0).getTitle()) {
                        case "five":
                            canvas.drawText(results.get(0).getTitle() + ": "+ Format.format(results.get(0).getConfidence()*100) +"%", x, y, fgPaint);
                            canvas.drawBitmap(bitmaps[0], 0, 10, fgPaint);
                            break;
                        case "fist":
                            canvas.drawText(results.get(0).getTitle() + ": "+ Format.format(results.get(0).getConfidence()*100) +"%", x, y, fgPaint);
                            canvas.drawBitmap(bitmaps[1], 0, 10, fgPaint);
                            break;
                        case "v shape":
                            canvas.drawText(results.get(0).getTitle() + ": "+ Format.format(results.get(0).getConfidence()*100) +"%", x, y, fgPaint);
                            canvas.drawBitmap(bitmaps[2], 0, 10, fgPaint);
                            break;
                        case "palm":
                            canvas.drawText(results.get(0).getTitle() + ": "+ Format.format(results.get(0).getConfidence()*100) +"%", x, y, fgPaint);
                            canvas.drawBitmap(bitmaps[3], 0, 10, fgPaint);

                            break;
                        case "c":
                            canvas.drawText(results.get(0).getTitle() + ": "+ Format.format(results.get(0).getConfidence()*100) +"%", x, y, fgPaint);
                            canvas.drawBitmap(bitmaps[4], 0, 10, fgPaint);
                            break;
                        case "point":
                            canvas.drawText(results.get(0).getTitle() + ": "+ Format.format(results.get(0).getConfidence()*100) +"%", x, y, fgPaint);
                            canvas.drawBitmap(bitmaps[5], 0, 10, fgPaint);
                            break;
                    }
                }
                y += fgPaint.getTextSize() * 1.5f;
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }


        y += fgPaint.getTextSize() * 1.5f;
    }
}
