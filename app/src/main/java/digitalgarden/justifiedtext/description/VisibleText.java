
package digitalgarden.justifiedtext.description;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import digitalgarden.justifiedtext.JigReader;

/**
 * Visible parts of the text
 * This can be started:
 * - before View is calculated (setFilePosition() can be called before)
 * - after View is calculated (setParameters() will be called after)
 */
public class VisibleText
    {
    // SOURCE - opened by constructor, should be closed by close()
    JigReader jigReader = null;


    // DEFAULT values
    private Paint fontPaint = new Paint();

    // Values get from view
    private int viewWidth = -1;
    private int viewHeight = -1;
    private int viewMargin = -1;


    private void checkNotClosed() throws IOException
        {
        if (jigReader == null)
            throw new IOException("JigReader already closed!");
        }


    public VisibleText( String fileName ) throws FileNotFoundException
        {
        File file = new File( Environment.getExternalStorageDirectory(), fileName );
        this.jigReader =
            new JigReader( file );

        setFontSize( 20f );
        setFontColor( Color.BLACK );
        }

    public void close() throws IOException
        {
        checkNotClosed();
        try
            {
            jigReader.close();
            }
        finally
            {
            jigReader = null;
            }
        }

    public void setFontTypeface(Typeface typeface)
        {
        fontPaint.setTypeface( typeface );
        }

    public void setFontSize( float textSize )
        {
        fontPaint.setTextSize( textSize );
        }

    public void setFontColor( int color )
        {
        fontPaint.setColor( color );
        }

    public void setViewParameters( int viewWidth, int viewHeight, int viewMargin )
        {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.viewMargin = viewMargin;

        // automatically show text, if textPosiion is given
        }

    public void setFilePosition( long filePosition ) throws IOException
        {
        checkNotClosed();

        firstLinePointer = filePosition;

        jigReader.seek( filePosition );

        int c;
        while( (c = jigReader.readByteBackward()) != -1)
            {
            if ( c == 0x0A )
                {
                jigReader.readByte();
                break;
                }
            }

        lastPosition = jigReader.getFilePointer();

        // At this point we are at the beginning of a paragraph, containing filePosition

        TextParagraph paragraph = new TextParagraph();
        lastPosition = paragraph.readParagraph( jigReader, lastPosition );

        paragraph.measureWords( fontPaint );
        paragraph.renderLines(viewWidth);
        visibleParagraphs.add( paragraph );

        // Find the first line
        if ( firstLine == -1 )
            {
            for ( int l = paragraph.sizeOfLines() - 1; l >0 ; l-- )
                {
                if ( firstLinePointer > paragraph.getLine( l ).getFilePointer() )
                    break;
                }

            firstLine = l;
            }



        }





    private List<TextParagraph> visibleParagraphs = new ArrayList<>();

    private long firstLinePointer;

    private int firstLine = -1;

    private long lastPosition;



    /**
     * At least first paragraph with valid first line is needed.
     * A new text is created from the first line
     */
    private void buildTextFromFirstLine()
        {
        TextParagraph paragraph;

        int paragraphCounter = 0;
        int lineCounter = firstLine - 1;

        int yPosition = viewMargin - fontAscent;

        while (yPosition < viewHeight - viewMargin - fontDescent)
            {
            lineCounter++;
            if (lineCounter >= visibleParagraphs.get(paragraphCounter).sizeOfLines())
                {
                lineCounter = 0;
                paragraphCounter++;
                if (paragraphCounter >= visibleParagraphs.size())
                    {
                    paragraph = new TextParagraph();
                    lastPosition = paragraph.readParagraph(jigReader, lastPosition);

                    paragraph.measureWords(fontPaint);
                    paragraph.renderLines(viewWidth);
                    visibleParagraphs.add(paragraph);

                    // ?? WHAT happens if no more paragraph is available ??
                    }
                }

            visibleParagraphs.get(paragraphCounter).getLine(lineCounter).setYPosition(yPosition);

            yPosition += lineHeight(visibleParagraphs.get(paragraphCounter).getLine(lineCounter).getType());
            }

        lastLine = lineCounter;

        paragraphCounter++;
        while (paragraphCounter < visibleParagraphs.size())
            {
            visibleParagraphs.remove(visibleParagraphs.size() - 1);
            }
        }

    public void drawText( Canvas canvas )
        {
        float positionY = -fontAscent;

        int paragraph = 0;
        int line = firstLineInView;

        while ( positionY < viewHeight)
            {
            while ( line >= visibleParagraphs.get(paragraph).sizeOfLines() )
                {
                paragraph++;
                if ( paragraph >= visibleParagraphs.size() )
                    return;
                line = 0;
                }

            int lineType = visibleParagraphs.get(paragraph).getLine(line).draw(canvas, positionY, fontPaint);
            if ( lineType == TextLine.LINE_NORMAL )
                positionY+= lineHeight;
            else if (lineType == TextLine.LINE_EMPTY )
                positionY += 10f;
            else // LINE_LAST
                positionY += lineHeight + 10f;

            line++;
            }
        }
    }
