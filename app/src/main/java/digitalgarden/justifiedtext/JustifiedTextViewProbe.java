package digitalgarden.justifiedtext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Just a probe
 */
public class JustifiedTextViewProbe extends View
    {
    /** How many lines can be in the View? */
    private int	linesInView = 0;

    /** Paint of the font */
    private Paint fontPaint;


    public JustifiedTextViewProbe(Context context)
        {
        super(context);
        init();
        }

    public JustifiedTextViewProbe(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        init();
        }

    public JustifiedTextViewProbe(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        init();
        }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
        setMeasuredDimension(measureIt(widthMeasureSpec), measureIt(heightMeasureSpec));
        }

    private void init()
        {
        fontPaint = new Paint();
        fontPaint.setColor(0xffffd4ab);
        setFontSize(26f);
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
        countLinesInView();
        }


    private void countLinesInView()
        {
        float fontAscent = fontPaint.ascent();
        float fontDescent = fontPaint.descent();
        float fontLeading = 5f;

        if ( linesInView >= 0 && getHeight() > 0f )
            linesInView = (int)(getHeight() / (-fontAscent + fontDescent + fontLeading)) + 1;
        // the last 'broken' line is needed, too
        }


    public void setFontSize( float size)
        {
        fontPaint.setTextSize( size );
        }

    @Override
    protected void onDraw(Canvas canvas)
        {
        Paragraph.Line line;
        Paragraph.Word word;

        int paraPos = 0;
        int linePos = firstLineInView;

        float posy = -fontAscent;

        for (int l=0; l < linesInView; l++)
            {
            line = paragraphs.get(paraPos).lines.get(linePos);
            line.posy = posy;
            for (int w=line.firstWord; w <= line.lastWord; w++)
                {
                word = paragraphs.get(paraPos).words.get(w);

                if (w == selectedWord && paraPos == selectedParagraph)
                    {
                    paintAround.setColor(Color.YELLOW);
                    around.set((int)word.posx, (int)(posy + fontAscent), (int)(word.posx + word.width), (int)(posy + fontDescent));
                    canvas.drawRect(around, paintAround);

                    paintFont.setColor( Color.YELLOW );

                    // Nagyítás
                    bigText = word.text;
                    if (posy < getHeight()/4)
                        bigy = getHeight() - 10f - bigFontDescent;
                    else
                        bigy = 10f - bigFontAscent;
                    rectBigFont.set(10, (int)(bigy + bigFontAscent), (int) (10 + paintBigFont.measureText(bigText)), (int)(bigy + bigFontDescent));
//Toast.makeText(getContext(), rectBigFont.toString(), Toast.LENGTH_SHORT).show();

                    }
                else
                    paintFont.setColor(0xffffd4ab);

                canvas.drawText(word.text, word.posx, posy, paintFont);
                }
            posy+= -fontAscent + fontDescent + fontLeading;

            linePos++;
            if (linePos >= paragraphs.get(paraPos).lines.size())
                {
                paraPos++;
                linePos = 0;
                }
            }
        }
    }
