package com.zhou.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

/**
 * Created by ZhOu on 2017/2/10.
 */

public class PwdEditText extends EditText {

    private Context mContext;

    private Paint paintBg, paintLine, paintCircle;

    private float mWidth, mHeight;

    private int pwd_length = 6, input_length = 0;

    private float pwd_width;

    private float pwd_size = 12;

    private float padding = 8;

    public PwdEditText(Context context) {
        this(context, null);
    }

    public PwdEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PwdEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        padding = context.getResources().getDisplayMetrics().density * padding;

        paintBg = new Paint();
        paintBg.setColor(Color.WHITE);
        paintBg.setAntiAlias(true);
        paintBg.setStyle(Paint.Style.FILL);

        paintLine = new Paint();
        paintLine.setColor(Color.GRAY);
        paintLine.setAntiAlias(true);
        paintLine.setStyle(Paint.Style.STROKE);

        paintCircle = new Paint();
        paintLine.setColor(Color.BLACK);
        paintLine.setAntiAlias(true);
        paintLine.setStyle(Paint.Style.FILL);

        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        setImeOptions(EditorInfo.IME_ACTION_DONE);
        setSingleLine(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rectf = new RectF(0, 0, mWidth, mHeight);
        canvas.drawRoundRect(rectf, 10, 10, paintBg);
        paintLine.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(new RectF(padding, padding, mWidth - padding, padding + pwd_width), 10, 10, paintLine);
        paintLine.setStyle(Paint.Style.FILL);
        for (int i = 1; i < pwd_length; i++) {
            float _x = i * pwd_width + padding;
            canvas.drawLine(_x, padding, _x, padding + pwd_width, paintLine);
        }

        float _halfWidth = pwd_width / 2;
        for (int i = 0; i < input_length; i++) {
            canvas.drawCircle(padding + _halfWidth + pwd_width * i, padding + _halfWidth, pwd_size, paintCircle);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        pwd_width = (mWidth - 2 * padding) / pwd_length;
        mHeight = pwd_width + 2 * padding;
        pwd_size = pwd_width / 4;
        setMeasuredDimension((int) mWidth, (int) mHeight);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        this.input_length = text.toString().length();
        if (input_length <= pwd_length) invalidate();
        if (pwd_length == input_length) {
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
