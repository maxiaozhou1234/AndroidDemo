package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;

import com.zhou.android.common.Tools;

/**
 * Created by ZhOu on 2017/2/10.
 */

public class VerifyCodeText extends AppCompatEditText {

    public final static int DEFAULT_COUNT = 6;
    public final static float DEFAULT_HORIZONTAL_SPACE = 10F;

    private Paint framePaint;
    private TextPaint textPaint;

    private int frameCount = DEFAULT_COUNT;
    private int inputCount = 0;
    private float frameWidth;
    private float horizontalSpace = DEFAULT_HORIZONTAL_SPACE;
    private float textHeight = 0f, textDif = 0f;
    private float textY = 0f;
    private float[] textWidths;

    public VerifyCodeText(Context context) {
        this(context, null);
    }

    public VerifyCodeText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifyCodeText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;
        horizontalSpace = density * horizontalSpace;

        framePaint = new Paint();
        framePaint.setColor(Color.parseColor("#A9A9A9"));
        framePaint.setAntiAlias(true);
        framePaint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(18f * density);

        textHeight = Math.abs(textPaint.getFontMetrics().ascent) + textPaint.getFontMetrics().descent;
        textDif = textHeight / 2 - textPaint.getFontMetrics().descent;

        textWidths = new float[frameCount];

        setFilters(new InputFilter[]{new InputFilter.LengthFilter(frameCount)});
        setCursorVisible(false);
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        setImeOptions(EditorInfo.IME_ACTION_DONE);
        setSingleLine(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        float l = getPaddingLeft();
        float t = getPaddingTop();
        float b = t + frameWidth;
        for (int i = 0; i < frameCount; i++) {
            float left = l + (frameWidth + horizontalSpace) * i;
            canvas.drawRect(left, t, left + frameWidth, b, framePaint);
        }

        if (inputCount > 0) {
            String text = getText().toString();
            char[] array = text.toCharArray();
            textPaint.getTextWidths(text, textWidths);
            for (int i = 0; i < inputCount; i++) {
                float x = l + (frameWidth + horizontalSpace) * i + (frameWidth - textWidths[i]) / 2;
                canvas.drawText(array, i, 1, x, textY, textPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        frameWidth = (width - getPaddingLeft() - getPaddingRight() - (frameCount - 1) * horizontalSpace) / frameCount;
        int height = (int) (frameWidth + getPaddingTop() + getPaddingBottom());
//        textY = (height + textHeight) / 2;
        textY = height / 2f + textDif;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.inputCount = text.toString().length();
        if (inputCount <= frameCount) invalidate();
        if (inputCount != 0 && frameCount == inputCount) {
            Tools.hideSoftInput(getContext(), this.getWindowToken());
            if (onFinishListener != null) {
                onFinishListener.onFinish(text.toString());
            }
        }
    }

    public interface OnFinishListener {
        void onFinish(String pwd);
    }

    private OnFinishListener onFinishListener;

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }
}
