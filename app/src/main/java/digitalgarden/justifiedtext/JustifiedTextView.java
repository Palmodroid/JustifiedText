package digitalgarden.justifiedtext;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import digitalgarden.justifiedtext.description.TextDescriptor;

/**
 * Just a probe
 */
public class JustifiedTextView extends View
    {
    private TextDescriptor textDescriptor = null;

    public JustifiedTextView(Context context)
        {
        super(context);
        }

    public JustifiedTextView(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public JustifiedTextView(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
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
        if ( textDescriptor != null )
            {
            textDescriptor.setViewParameters( width, height, 0 );
            }
        }


    public void setVisibleText(TextDescriptor textDescriptor, long startPointer )
        {
        this.textDescriptor = textDescriptor;
        if ( getHeight() > 0 )
            {
            this.textDescriptor.setViewParameters( getWidth(), getHeight(), 0 );
            }
        this.textDescriptor.setFilePointer( startPointer );
        }

    @Override
    protected void onDraw(Canvas canvas)
        {
        if ( textDescriptor != null )
            {
            textDescriptor.drawText( canvas );
            }
        }
    }
