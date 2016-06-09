package digitalgarden.justifiedtext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import digitalgarden.justifiedtext.description.VisibleText;

/**
 * Just a probe
 */
public class JustifiedTextView extends View
    {
    private VisibleText visibleText = null;

    /** Paint of the font */
    private Paint fontPaint;


    public JustifiedTextView(Context context)
        {
        super(context);
        init();
        }

    public JustifiedTextView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        init();
        }

    public JustifiedTextView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        init();
        }

    private void init()
        {
        fontPaint = new Paint();
        fontPaint.setColor(0xffffd4ab);
        setFontSize(26f);
        }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
        setMeasuredDimension(measureIt(widthMeasureSpec), measureIt(heightMeasureSpec));
        }

    /**
     * View tries to occupy the biggest available area
     * - MeasureSpec.EXACTLY - returns the exact size
     * - MeasureSpec.AT_MOST - returns the biggest available size
     * - else (MeasureSpec.UNSPECIFIED) - returns the biggest size (integer)
     * @param measureSpec onMeasure parameter
     * @return biggest available size
     */
    private int measureIt(int measureSpec)
        {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        return ( specMode == MeasureSpec.EXACTLY || specMode == MeasureSpec.AT_MOST ) ?
                specSize : Integer.MAX_VALUE;
        }


    /**
     * onSizeChanged is called only once, when View size is already calculated
     */
    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
        {
        if ( visibleText != null )
            {
            visibleText.setParameters( fontPaint, width, height );
            }
        }


    public void setVisibleText( VisibleText visibleText, int firstParagraph, int firstWord )
        {
        this.visibleText = visibleText;
        this.visibleText.setFilePosition( firstParagraph, firstWord );

        if ( getHeight() > 0 ) //
            {
            this.visibleText.setParameters( fontPaint, getWidth(), getHeight() );
            }
        }

    public void setFontSize( float size)
        {
        fontPaint.setTextSize( size );
        }

    @Override
    protected void onDraw(Canvas canvas)
        {
        if ( visibleText != null )
            {
            visibleText.drawText( canvas );
            }
        }
    }
