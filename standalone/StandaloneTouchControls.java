package javax.microedition.lcdui.keyboard;

import android.graphics.RectF;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.graphics.CanvasWrapper;
import ru.playsoftware.j2meloader.config.ProfileModel;

/** Standalone controls: circular four-way joystick and numeric 5 key. */
public final class StandaloneTouchControls extends VirtualKeyboard {
    private Canvas target;
    private float joyCx, joyCy, joyRadius;
    private float fiveCx, fiveCy, fiveRadius;
    private int joyKey;
    private boolean fiveDown;

    public StandaloneTouchControls(ProfileModel settings) { super(settings); }

    @Override public void setTarget(Canvas canvas) { target = canvas; }

    @Override public void resize(RectF realScreen, float l, float t, float r, float b) {
        float w = Math.max(1, r - l), h = Math.max(1, b - t);
        float size = Math.min(w, h);
        joyRadius = size * 0.115f;
        joyCx = l + size * 0.17f;
        joyCy = b - size * 0.17f;
        fiveRadius = size * 0.075f;
        fiveCx = r - size * 0.12f;
        fiveCy = b - size * 0.12f;
    }

    private boolean inJoy(float x, float y) {
        float dx = x - joyCx, dy = y - joyCy;
        return dx * dx + dy * dy <= joyRadius * joyRadius;
    }

    private boolean inFive(float x, float y) {
        float dx = x - fiveCx, dy = y - fiveCy;
        return dx * dx + dy * dy <= fiveRadius * fiveRadius;
    }

    private int direction(float x, float y) {
        float dx = x - joyCx, dy = y - joyCy;
        float dead = joyRadius * 0.28f;
        if (dx * dx + dy * dy <= dead * dead) return 0;
        return Math.abs(dx) > Math.abs(dy) ? (dx < 0 ? Canvas.KEY_LEFT : Canvas.KEY_RIGHT)
                : (dy < 0 ? Canvas.KEY_UP : Canvas.KEY_DOWN);
    }

    private void setJoy(int key) {
        if (target == null || key == joyKey) return;
        if (joyKey != 0) target.postKeyReleased(joyKey);
        joyKey = key;
        if (joyKey != 0) target.postKeyPressed(joyKey);
    }

    @Override public boolean pointerPressed(int pointer, float x, float y) {
        if (inJoy(x, y)) { setJoy(direction(x, y)); return true; }
        if (inFive(x, y)) {
            if (!fiveDown && target != null) target.postKeyPressed(Canvas.KEY_NUM5);
            fiveDown = true;
            return true;
        }
        return false;
    }

    @Override public boolean pointerDragged(int pointer, float x, float y) {
        if (inJoy(x, y)) { setJoy(direction(x, y)); return true; }
        if (joyKey != 0) setJoy(0);
        if (inFive(x, y)) {
            if (!fiveDown && target != null) target.postKeyPressed(Canvas.KEY_NUM5);
            fiveDown = true;
            return true;
        }
        return true;
    }

    @Override public boolean pointerReleased(int pointer, float x, float y) {
        setJoy(0);
        if (fiveDown && target != null) target.postKeyReleased(Canvas.KEY_NUM5);
        fiveDown = false;
        return true;
    }

    @Override public boolean keyPressed(int keyCode) { return false; }
    @Override public boolean keyRepeated(int keyCode) { return false; }
    @Override public boolean keyReleased(int keyCode) { return false; }

    @Override public void paint(CanvasWrapper g) {
        if (target == null) return;
        float r = joyRadius;
        g.setColorAlpha(0x66000000);
        g.fillArc((int)(joyCx-r), (int)(joyCy-r), (int)(2*r), (int)(2*r), 0, 360);
        g.setColorAlpha(0xCCFFFFFF);
        g.drawArc((int)(joyCx-r), (int)(joyCy-r), (int)(2*r), (int)(2*r), 0, 360);
        float k = r * 0.43f, kx = joyCx, ky = joyCy;
        if (joyKey == Canvas.KEY_LEFT) kx -= r * .34f;
        else if (joyKey == Canvas.KEY_RIGHT) kx += r * .34f;
        else if (joyKey == Canvas.KEY_UP) ky -= r * .34f;
        else if (joyKey == Canvas.KEY_DOWN) ky += r * .34f;
        g.setColorAlpha(0xBBFFFFFF);
        g.fillArc((int)(kx-k), (int)(ky-k), (int)(2*k), (int)(2*k), 0, 360);
        float f = fiveRadius;
        g.setColorAlpha(fiveDown ? 0xCCFFFFFF : 0x88000000);
        g.fillArc((int)(fiveCx-f), (int)(fiveCy-f), (int)(2*f), (int)(2*f), 0, 360);
        g.setColorAlpha(0xCCFFFFFF);
        g.drawArc((int)(fiveCx-f), (int)(fiveCy-f), (int)(2*f), (int)(2*f), 0, 360);
        g.setColorAlpha(fiveDown ? 0xFF000000 : 0xFFFFFFFF);
        g.drawString("5", (int)fiveCx, (int)fiveCy, Graphics.HCENTER | Graphics.VCENTER);
    }
}
