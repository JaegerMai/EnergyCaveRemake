package com.powerrun.akenergycaveremake.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TemperatureGaugeView extends View {
    private int currentTemp;
    private int minTemp;
    private int maxTemp;
    private Paint paint;

    public TemperatureGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        minTemp = 20;
        maxTemp = 70;
        currentTemp = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = (int) (Math.min(width, height) / 2 * 0.9);
        int centerX = width / 2;
        int centerY = height / 2;

        // Define the bounds for the arc
        RectF bounds = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw the half circle
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        canvas.drawArc(bounds, -234, 72, false, paint);
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        canvas.drawArc(bounds, -162, 144, false, paint);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        canvas.drawArc(bounds, -18, 72, false, paint);

        // Draw the pointer
        float angle = (float) (currentTemp - minTemp) / (maxTemp - minTemp) * 360;
        float endX = centerX + radius * (float) Math.cos(Math.toRadians(angle - 234));
        float endY = centerY + radius * (float) Math.sin(Math.toRadians(angle - 234));
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(8);
        canvas.drawLine(centerX, centerY, endX, endY, paint);
    }

    public void setCurrentTemp(int currentTemp) {
        this.currentTemp = currentTemp;
        invalidate();
    }
}