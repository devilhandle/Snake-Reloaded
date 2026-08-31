package javax.microedition.shell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/** Four-key neon HUD controls for the standalone J2ME build. */
public final class StandaloneTouchControls extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow = new Path();
    private float density;
    private float buttonW, buttonH;
    private float leftX, rightX, centerX, upY, downY, sideY;
    private int activeKey;

    public StandaloneTouchControls(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setFocusable(false);
        setClickable(true);
        fill.setDither(true);
        stroke.setDither(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float min = Math.min(w, h);
        buttonW = Math.max(92f * density, min * 0.25f);
        buttonH = Math.max(48f * density, min * 0.105f);
        sideY = h - buttonH * 1.55f;
        centerX = w * 0.50f;
        leftX = w * 0.18f;
        rightX = w * 0.82f;
        upY = sideY - buttonH * 0.72f;
        downY = sideY + buttonH * 0.72f;
    }

    private boolean inside(float x, float y, float cx, float cy) {
        float halfW = buttonW * 0.50f;
        float halfH = buttonH * 0.50f;
        return x >= cx - halfW && x <= cx + halfW
                && y >= cy - halfH && y <= cy + halfH;
    }

    private int keyAt(float x, float y) {
        if (inside(x, y, centerX, upY)) return KeyEvent.KEYCODE_DPAD_UP;
        if (inside(x, y, centerX, downY)) return KeyEvent.KEYCODE_DPAD_DOWN;
        if (inside(x, y, leftX, sideY)) return KeyEvent.KEYCODE_DPAD_LEFT;
        if (inside(x, y, rightX, sideY)) return KeyEvent.KEYCODE_DPAD_RIGHT;
        return 0;
    }

    private void send(int action, int key) {
        if (key != 0) getRootView().dispatchKeyEvent(new KeyEvent(action, key));
    }

    private void releaseActive() {
        if (activeKey != 0) {
            send(KeyEvent.ACTION_UP, activeKey);
            activeKey = 0;
        }
    }

    private void roundedButton(Canvas canvas, float cx, float cy, boolean pressed) {
        float l = cx - buttonW * 0.5f;
        float t = cy - buttonH * 0.5f;
        float r = cx + buttonW * 0.5f;
        float b = cy + buttonH * 0.5f;
        float radius = buttonH * 0.24f;

        fill.setStyle(Paint.Style.FILL);
        fill.setShader(new LinearGradient(0, t, 0, b,
                pressed ? 0xAA3A7F92 : 0x88406474,
                0x55301D48, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(l, t, r, b, radius, radius, fill);
        fill.setShader(null);

        fill.setColor(pressed ? 0x9947B8D0 : 0x66213A4A);
        canvas.drawRoundRect(l + 4, t + 4, r - 4, b - 4,
                radius * 0.82f, radius * 0.82f, fill);

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(2f, density * 2f));
        stroke.setColor(pressed ? 0xFFB8FFFF : 0xFF62E8FF);
        canvas.drawRoundRect(l + 1, t + 1, r - 1, b - 1, radius, radius, stroke);
        stroke.setStrokeWidth(Math.max(1f, density));
        stroke.setColor(0xFF8A4EAE);
        canvas.drawRoundRect(l + 5, t + 5, r - 5, b - 5,
                radius * 0.82f, radius * 0.82f, stroke);

        stroke.setStrokeWidth(Math.max(1f, density));
        stroke.setColor(0x5562E8FF);
        for (int i = 1; i < 5; i++) {
            float yy = t + buttonH * (i / 5f);
            canvas.drawLine(l + 10, yy, r - 10, yy, stroke);
        }
    }

    private void drawArrow(Canvas canvas, float cx, float cy, int direction, boolean pressed) {
        float s = Math.min(buttonW, buttonH) * 0.25f;
        float shaft = s * 0.68f;
        float head = s * 0.95f;

        arrow.reset();
        if (direction == KeyEvent.KEYCODE_DPAD_LEFT) {
            arrow.moveTo(cx - head, cy);
            arrow.lineTo(cx - shaft * 0.15f, cy - s);
            arrow.lineTo(cx - shaft * 0.15f, cy - shaft * 0.45f);
            arrow.lineTo(cx + head * 0.55f, cy - shaft * 0.45f);
            arrow.lineTo(cx + head * 0.55f, cy + shaft * 0.45f);
            arrow.lineTo(cx - shaft * 0.15f, cy + shaft * 0.45f);
            arrow.lineTo(cx - shaft * 0.15f, cy + s);
            arrow.close();
        } else if (direction == KeyEvent.KEYCODE_DPAD_RIGHT) {
            arrow.moveTo(cx + head, cy);
            arrow.lineTo(cx + shaft * 0.15f, cy - s);
            arrow.lineTo(cx + shaft * 0.15f, cy - shaft * 0.45f);
            arrow.lineTo(cx - head * 0.55f, cy - shaft * 0.45f);
            arrow.lineTo(cx - head * 0.55f, cy + shaft * 0.45f);
            arrow.lineTo(cx + shaft * 0.15f, cy + shaft * 0.45f);
            arrow.lineTo(cx + shaft * 0.15f, cy + s);
            arrow.close();
        } else if (direction == KeyEvent.KEYCODE_DPAD_UP) {
            arrow.moveTo(cx, cy - head);
            arrow.lineTo(cx - s, cy + shaft * 0.15f);
            arrow.lineTo(cx - shaft * 0.45f, cy + shaft * 0.15f);
            arrow.lineTo(cx - shaft * 0.45f, cy + head * 0.55f);
            arrow.lineTo(cx + shaft * 0.45f, cy + head * 0.55f);
            arrow.lineTo(cx + shaft * 0.45f, cy + shaft * 0.15f);
            arrow.lineTo(cx + s, cy + shaft * 0.15f);
            arrow.close();
        } else {
            arrow.moveTo(cx, cy + head);
            arrow.lineTo(cx - s, cy - shaft * 0.15f);
            arrow.lineTo(cx - shaft * 0.45f, cy - shaft * 0.15f);
            arrow.lineTo(cx - shaft * 0.45f, cy - head * 0.55f);
            arrow.lineTo(cx + shaft * 0.45f, cy - head * 0.55f);
            arrow.lineTo(cx + shaft * 0.45f, cy - shaft * 0.15f);
            arrow.lineTo(cx + s, cy - shaft * 0.15f);
            arrow.close();
        }

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(2f, density * 2.2f));
        stroke.setColor(pressed ? 0xFFFFFFFF : 0xFF6FF5FF);
        stroke.setShadowLayer(pressed ? 8f * density : 5f * density, 0, 0, 0xAA35DFFF);
        setLayerType(View.LAYER_TYPE_SOFTWARE, stroke);
        canvas.drawPath(arrow, stroke);
        stroke.clearShadowLayer();

        fill.setStyle(Paint.Style.FILL);
        fill.setColor(pressed ? 0x6635DFFF : 0x331FEAFF);
        canvas.drawPath(arrow, fill);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean up = activeKey == KeyEvent.KEYCODE_DPAD_UP;
        boolean down = activeKey == KeyEvent.KEYCODE_DPAD_DOWN;
        boolean left = activeKey == KeyEvent.KEYCODE_DPAD_LEFT;
        boolean right = activeKey == KeyEvent.KEYCODE_DPAD_RIGHT;

        roundedButton(canvas, centerX, upY, up);
        roundedButton(canvas, centerX, downY, down);
        roundedButton(canvas, leftX, sideY, left);
        roundedButton(canvas, rightX, sideY, right);

        drawArrow(canvas, centerX, upY, KeyEvent.KEYCODE_DPAD_UP, up);
        drawArrow(canvas, centerX, downY, KeyEvent.KEYCODE_DPAD_DOWN, down);
        drawArrow(canvas, leftX, sideY, KeyEvent.KEYCODE_DPAD_LEFT, left);
        drawArrow(canvas, rightX, sideY, KeyEvent.KEYCODE_DPAD_RIGHT, right);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            int key = keyAt(x, y);
            if (key != 0) {
                activeKey = key;
                send(KeyEvent.ACTION_DOWN, activeKey);
                invalidate();
                return true;
            }
            return false;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            int key = keyAt(x, y);
            if (key != activeKey) {
                releaseActive();
                if (key != 0) {
                    activeKey = key;
                    send(KeyEvent.ACTION_DOWN, activeKey);
                }
                invalidate();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            releaseActive();
            invalidate();
            return true;
        }
        return true;
    }
}
