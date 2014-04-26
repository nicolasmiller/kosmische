package net.sf.supercollider.android;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import java.lang.Math;
import android.util.Log;

public class Knob extends KosmischeWidget {
    private float radius;
    private final float THREE_PI_OVER_TWO = (float) ((3 * Math.PI) / 2);
    private final float TWO_PI = (float) (2 * Math.PI);
    private final float COS_PI_4 = (float) Math.cos(Math.PI / 4);
    private final float SIN_PI_4 = (float) Math.sin(Math.PI / 4);

    public Knob(Context context) {
        super(context);
        this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if ((event.getAction() == MotionEvent.ACTION_DOWN) || (event.getAction() == MotionEvent.ACTION_MOVE)) {
                        float dx = event.getX() - centerX;
                        float dy = event.getY() - centerY;
                        if(((dx * dx) + (dy * dy)) < (radius * radius)) {
                            float next_position = THREE_PI_OVER_TWO - normalized_atan2(dx / radius, dy / radius);
                            if(next_position >= 0)
                                position = next_position / THREE_PI_OVER_TWO;
                            invalidate();
                        }
                    }
                    return true;
                }
            });
    }

    private float effectiveWidth() {
        return width < height ? width : height;
    }

    private float normalized_atan2(float y, float x) {
        float x_prime = COS_PI_4 * x + SIN_PI_4 * y;
        float y_prime = -SIN_PI_4 * x + COS_PI_4 * y;
        float angle = (float) Math.atan2(y_prime, x_prime);
        if(angle < 0) {
            return (angle + (TWO_PI));
        }
        return angle;
    }

    protected void drawOutline(Canvas canvas) {
        Paint arcColor = new Paint();
        arcColor.setARGB(255, 255, 0, 0);
        arcColor.setStyle(Paint.Style.STROKE);
        canvas.drawArc(new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius), 135, 270, false, arcColor);
        canvas.drawArc(new RectF(centerX - (radius / 2), centerY - (radius / 2), centerX + (radius / 2), centerY + (radius / 2)), 135, 270, false, arcColor);
        float r_offset = (float) (radius / Math.sqrt(2));
        canvas.drawLine(centerX, centerY, centerX + r_offset, centerY + r_offset, arcColor);
        canvas.drawLine(centerX, centerY, centerX - r_offset, centerY + r_offset, arcColor);
    }

    protected void drawFill(Canvas canvas) {
        Paint wedgeColor = new Paint();
        wedgeColor.setARGB(255, red, green, blue);
        canvas.drawArc(new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius), 135, (int) (270 * position), true, wedgeColor);

        Paint background = new Paint();
        background.setARGB(255, 0, 0, 0);
        canvas.drawCircle(centerX, centerY, (radius / 2) - 1, background);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        radius = effectiveWidth() / 2;
        drawOutline(canvas);
        drawFill(canvas);
        drawLabel(canvas);
    }
}
