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

/** Two-button left/right controls plus a dedicated 5 key. */
public final class StandaloneTouchControls extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow = new Path();
    private float density;
    private float buttonW, buttonH;
    private float leftX, rightX, centerY;
    private float fiveX, fiveY, fiveSize;
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
        buttonW = Math.max(150f * density, min * 0.38f);
        buttonH = Math.max(58f * density, min * 0.14f);
        centerY = h - buttonH * 1.25f;
        leftX = w * 0.25f;
        rightX = w * 0.75f;
        fiveSize = buttonH * 0.78f;
        fiveX = rightX + buttonW * 0.30f;
        fiveY = centerY - buttonH * 1.25f;
    }

    private boolean inside(float x, float y, float cx) {
        float halfW = buttonW * 0.50f;
        float halfH = buttonH * 0.50f;
        return x >= cx - halfW && x <= cx + halfW
                && y >= centerY - halfH && y <= centerY + halfH;
    }

    private boolean insideFive(float x, float y) {
        float half = fiveSize * 0.50f;
        return x >= fiveX - half && x <= fiveX + half
                && y >= fiveY - half && y <= fiveY + half;
    }

    private int keyAt(float x, float y) {
        if (insideFive(x, y)) return KeyEvent.KEYCODE_5;
        if (inside(x, y, leftX)) return KeyEvent.KEYCODE_DPAD_LEFT;
        if (inside(x, y, rightX)) return KeyEvent.KEYCODE_DPAD_RIGHT;
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

    private void roundedButton(Canvas canvas, float cx, boolean pressed) {
        float l = cx - buttonW * 0.5f;
        float t = centerY - buttonH * 0.5f;
        float r = cx + buttonW * 0.5f;
        float b = centerY + buttonH * 0.5f;
        float radius = buttonH * 0.22f;

        fill.setStyle(Paint.Style.FILL);
        fill.setShader(new LinearGradient(0, t, 0, b,
                pressed ? 0xCC3F8397 : 0x99456A76,
                0x66301D49, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(l, t, r, b, radius, radius, fill);
        fill.setShader(null);

        fill.setColor(pressed ? 0xAA46B9D3 : 0x7720394A);
        canvas.drawRoundRect(l + 5, t + 5, r - 5, b - 5,
                radius * 0.78f, radius * 0.78f, fill);

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(2f, density * 2.2f));
        stroke.setColor(pressed ? 0xFFFFFFFF : 0xFF63EFFF);
        stroke.setShadowLayer(pressed ? 8f * density : 4f * density,
                0, 0, 0xAA35DFFF);
        setLayerType(View.LAYER_TYPE_SOFTWARE, stroke);
        canvas.drawRoundRect(l + 1, t + 1, r - 1, b - 1, radius, radius, stroke);
        stroke.clearShadowLayer();

        stroke.setStrokeWidth(Math.max(1f, density));
        stroke.setColor(0xFF874DAA);
        canvas.drawRoundRect(l + 6, t + 6, r - 6, b - 6,
                radius * 0.78f, radius * 0.78f, stroke);

        stroke.setStrokeWidth(Math.max(1f, density));
        stroke.setColor(0x5562E8FF);
        for (int i = 1; i < 5; i++) {
            float yy = t + buttonH * (i / 5f);
            canvas.drawLine(l + 14, yy, r - 14, yy, stroke);
        }
    }

    private void drawArrow(Canvas canvas, float cx, int direction, boolean pressed) {
        float s = Math.min(buttonW, buttonH) * 0.34f;
        float shaft = s * 0.62f;
        float head = s * 0.92f;

        arrow.reset();
        if (direction == KeyEvent.KEYCODE_DPAD_LEFT) {
            arrow.moveTo(cx - head, centerY);
            arrow.lineTo(cx - shaft * 0.10f, centerY - s);
            arrow.lineTo(cx - shaft * 0.10f, centerY - shaft * 0.42f);
            arrow.lineTo(cx + head * 0.55f, centerY - shaft * 0.42f);
            arrow.lineTo(cx + head * 0.55f, centerY + shaft * 0.42f);
            arrow.lineTo(cx - shaft * 0.10f, centerY + shaft * 0.42f);
            arrow.lineTo(cx - shaft * 0.10f, centerY + s);
        } else {
            arrow.moveTo(cx + head, centerY);
            arrow.lineTo(cx + shaft * 0.10f, centerY - s);
            arrow.lineTo(cx + shaft * 0.10f, centerY - shaft * 0.42f);
            arrow.lineTo(cx - head * 0.55f, centerY - shaft * 0.42f);
            arrow.lineTo(cx - head * 0.55f, centerY + shaft * 0.42f);
            arrow.lineTo(cx + shaft * 0.10f, centerY + shaft * 0.42f);
            arrow.lineTo(cx + shaft * 0.10f, centerY + s);
        }
        arrow.close();

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(2.5f, density * 2.8f));
        stroke.setColor(pressed ? 0xFFFFFFFF : 0xFF70F7FF);
        stroke.setShadowLayer(pressed ? 9f * density : 6f * density,
                0, 0, 0xAA35DFFF);
        setLayerType(View.LAYER_TYPE_SOFTWARE, stroke);
        canvas.drawPath(arrow, stroke);
        stroke.clearShadowLayer();

        fill.setStyle(Paint.Style.FILL);
        fill.setColor(pressed ? 0x7735DFFF : 0x4420EAFF);
        canvas.drawPath(arrow, fill);
    }

    private void drawFive(Canvas canvas, boolean pressed) {
        float half = fiveSize * 0.50f;
        float l = fiveX - half;
        float t = fiveY - half;
        float r = fiveX + half;
        float b = fiveY + half;
        float radius = fiveSize * 0.16f;

        fill.setStyle(Paint.Style.FILL);
        fill.setShader(new LinearGradient(0, t, 0, b,
                pressed ? 0xFFE6F2A0 : 0xCCB7D56A,
                pressed ? 0xFF657C36 : 0x884B602B, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(l, t, r, b, radius, radius, fill);
        fill.setShader(null);

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(Math.max(2f, density * 2f));
        stroke.setColor(pressed ? 0xFFFFFFFF : 0xFFE5FF91);
        stroke.setShadowLayer(pressed ? 8f * density : 4f * density,
                0, 0, 0xAAE8FF65);
        setLayerType(View.LAYER_TYPE_SOFTWARE, stroke);
        canvas.drawRoundRect(l + 1, t + 1, r - 1, b - 1, radius, radius, stroke);
        stroke.clearShadowLayer();

        fill.setStyle(Paint.Style.FILL);
        fill.setColor(pressed ? 0xFFFFFFFF : 0xFFF4F7D0);
        fill.setTextSize(fiveSize * 0.58f);
        fill.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = fill.getFontMetrics();
        float baseline = fiveY - (fm.ascent + fm.descent) * 0.5f;
        canvas.drawText("5", fiveX, baseline, fill);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean left = activeKey == KeyEvent.KEYCODE_DPAD_LEFT;
        boolean right = activeKey == KeyEvent.KEYCODE_DPAD_RIGHT;
        boolean five = activeKey == KeyEvent.KEYCODE_5;

        roundedButton(canvas, leftX, left);
        roundedButton(canvas, rightX, right);
        drawArrow(canvas, leftX, KeyEvent.KEYCODE_DPAD_LEFT, left);
        drawArrow(canvas, rightX, KeyEvent.KEYCODE_DPAD_RIGHT, right);
        drawFive(canvas, five);
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
