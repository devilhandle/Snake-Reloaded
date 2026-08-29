package javax.microedition.shell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/** White portrait D-pad controls plus the numeric 5 key. */
public final class StandaloneTouchControls extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private float cx, cy, button;
    private float fiveX, fiveY;
    private int activeKey;
    private boolean fiveDown;

    public StandaloneTouchControls(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setFocusable(false);
        setClickable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float min = Math.min(w, h);
        button = Math.max(30f * density, min * 0.075f);
        cx = w * 0.18f;
        cy = h * 0.80f;
        fiveX = w * 0.84f;
        fiveY = h * 0.80f;
    }

    private int directionFor(float x, float y) {
        float dx = x - cx;
        float dy = y - cy;
        float reach = button * 0.92f;
        if (Math.abs(dx) <= reach && dy <= -button * 0.18f && dy >= -button * 2.0f)
            return KeyEvent.KEYCODE_DPAD_UP;
        if (Math.abs(dx) <= reach && dy >= button * 0.18f && dy <= button * 2.0f)
            return KeyEvent.KEYCODE_DPAD_DOWN;
        if (Math.abs(dy) <= reach && dx <= -button * 0.18f && dx >= -button * 2.0f)
            return KeyEvent.KEYCODE_DPAD_LEFT;
        if (Math.abs(dy) <= reach && dx >= button * 0.18f && dx <= button * 2.0f)
            return KeyEvent.KEYCODE_DPAD_RIGHT;
        return 0;
    }

    private boolean inFive(float x, float y) {
        float r = button * 0.82f;
        float dx = x - fiveX, dy = y - fiveY;
        return dx * dx + dy * dy <= r * r;
    }

    private void send(int action, int key) {
        if (key != 0) getRootView().dispatchKeyEvent(new KeyEvent(action, key));
    }

    private void releaseDirection() {
        if (activeKey != 0) {
            send(KeyEvent.ACTION_UP, activeKey);
            activeKey = 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFFFFFFF);
        paint.setAlpha(205);
        canvas.drawCircle(cx, cy - button * 1.08f, button * 0.70f, paint);
        canvas.drawCircle(cx, cy + button * 1.08f, button * 0.70f, paint);
        canvas.drawCircle(cx - button * 1.08f, cy, button * 0.70f, paint);
        canvas.drawCircle(cx + button * 1.08f, cy, button * 0.70f, paint);

        paint.setColor(0xFF000000);
        paint.setAlpha(255);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(button * 0.72f);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float off = -(fm.ascent + fm.descent) * 0.5f;
        canvas.drawText("↑", cx, cy - button * 1.08f + off, paint);
        canvas.drawText("↓", cx, cy + button * 1.08f + off, paint);
        canvas.drawText("←", cx - button * 1.08f, cy + off, paint);
        canvas.drawText("→", cx + button * 1.08f, cy + off, paint);

        paint.setColor(0xFFFFFFFF);
        paint.setAlpha(fiveDown ? 255 : 205);
        canvas.drawCircle(fiveX, fiveY, button * 0.82f, paint);
        paint.setColor(0xFF000000);
        paint.setAlpha(255);
        paint.setTextSize(button * 0.65f);
        canvas.drawText("5", fiveX, fiveY + off, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            if (inFive(x, y)) {
                fiveDown = true;
                send(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_5);
                invalidate();
                return true;
            }
            int key = directionFor(x, y);
            if (key != 0) {
                activeKey = key;
                send(KeyEvent.ACTION_DOWN, activeKey);
                invalidate();
                return true;
            }
            return false;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (fiveDown) return true;
            int key = directionFor(x, y);
            if (key != activeKey) {
                releaseDirection();
                if (key != 0) {
                    activeKey = key;
                    send(KeyEvent.ACTION_DOWN, activeKey);
                }
                invalidate();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            releaseDirection();
            if (fiveDown) {
                send(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_5);
                fiveDown = false;
            }
            invalidate();
            return true;
        }
        return true;
    }
}
