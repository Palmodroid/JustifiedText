
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

import digitalgarden.justifiedtext.jigreader.JigReader;
import digitalgarden.justifiedtext.scribe.Scribe;

/**
 * Visible parts of the text
 * This can be started:
 * - before View is calculated (setFilePosition() can be called before)
 * - after View is calculated (setParameters() will be called after)
 */
public class TextDescriptor
    {
    // SOURCE - opened by constructor, should be closed by close()
    JigReader jigReader = null;


    // DEFAULT values
    private Paint fontPaint = new Paint();

    // Values of dimensions
    private int fontAscent;
    private int fontDescent;
    private int lineSpace;
    private int lastLineSpace;
    private int emptyLineSpace;

    // Values get from view
    private int viewWidth = -1;
    private int viewHeight = -1;
    private int viewMargin = -1;


    // visible paragraphs ONLY!
    private List<ParaDescriptor> visibleParas = new ArrayList<>();
    // first visible line of FIRST paragraph
    private int firstLine = -1;
    // last visible line of LAST paragraph
    private int lastLine = -1;

    // pointer right after the last paragraph
    private long lastFilePointer;





    private void checkNotClosed() throws IOException
        {
        if (jigReader == null)
            throw new IOException("JigReader already closed!");
        }


    private boolean isViewAndFileDataReady()
        {
        return firstLine >= 0 && viewHeight >= 0;
        }


    public TextDescriptor(String fileName ) throws FileNotFoundException
        {
        File file = new File( Environment.getExternalStorageDirectory(), fileName );
        this.jigReader =
            new JigReader( file );

        Scribe.debug("TextDescriptor file: [" + file.getAbsolutePath() + "] was opened.");

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
        setFontDimensionData();
        }

    public void setFontSize( float textSize )
        {
        fontPaint.setTextSize( textSize );
        setFontDimensionData();
        }

    private void setFontDimensionData()
        {
        fontAscent = (int)fontPaint.ascent();
        fontDescent = (int)fontPaint.descent();
        lineSpace = 5;
        lastLineSpace = 10;
        emptyLineSpace = 20;
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

        buildTextFromFirstLine();
        }


    /**
     * Paragraph is read at the end of the paragraphs
     * @return file pointer for the next paragraph, or -1L, if eof is reached
     */
    private long readNextParagraph( )
        {
        if ( lastFilePointer < 0L )
            return lastFilePointer;

        ParaDescriptor paragraph = new ParaDescriptor();
        lastFilePointer = paragraph.readPara( jigReader, lastFilePointer);
        paragraph.measureWords( fontPaint );
        paragraph.renderLines(viewWidth);
        visibleParas.add( paragraph );

        return lastFilePointer;
        }


    public void setFilePosition( long filePointer ) throws IOException
        {
        checkNotClosed();

        long firstLinePointer = filePointer;

        // Find beginning of paragraph
        jigReader.seek( filePointer );
        int c;
        while( (c = jigReader.readByteBackward()) != -1)
            {
            if ( c == 0x0A )
                {
                lastFilePointer = jigReader.getFilePointer() + 1;
                break;
                }
            }

        // Read first paragraph
        visibleParas.clear();
        readNextParagraph( );

        // Find the first line
        int l = visibleParas.get(0).sizeOfLines() - 1; // at least 0
        while ( l > 0 )
            {
            if ( firstLinePointer > visibleParas.get(0).getLine( l ).getFilePointer() )
                break;
            }

        firstLine = l;

        buildTextFromFirstLine();
        }


    /**
     * At least first paragraph with valid first line is needed.
     * A new text is created from the first line
     */
    private void buildTextFromFirstLine()
        {
        if ( !isViewAndFileDataReady() )
            return;

        ParaDescriptor paragraph;

        int paraCounter = 0;
        int lineCounter = firstLine - 1;

        int positionY = viewMargin - fontAscent;

        while (positionY < viewHeight - viewMargin - fontDescent)
            {
            lineCounter++;
            if (lineCounter >= visibleParas.get(paraCounter).sizeOfLines())
                {
                lineCounter = 0;
                paraCounter++;
                if (paraCounter >= visibleParas.size())
                    {
                    paragraph = new ParaDescriptor();
                    lastFilePointer = paragraph.readPara(jigReader, lastFilePointer);

                    paragraph.measureWords(fontPaint);
                    paragraph.renderLines(viewWidth);
                    visibleParas.add(paragraph);

                    // ?? WHAT happens if no more paragraph is available ??
                    }
                }

            visibleParas.get(paraCounter).getLine(lineCounter).setPositionY(positionY);
            positionY += getLineHeight(paraCounter, lineCounter);
            }

        lastLine = lineCounter;

        paraCounter++;
        while (paraCounter < visibleParas.size())
            {
            visibleParas.remove(visibleParas.size() - 1);
            }
        }


    private int getLineHeight( int paragraphCounter, int lineCounter )
        {
        if ( visibleParas.get(paragraphCounter).getLine( lineCounter).isEmpty() )
            {
            return emptyLineSpace;
            }

        if ( visibleParas.get(paragraphCounter).getLine( lineCounter).isLast() )
            {
            return fontDescent - fontAscent + lastLineSpace;
            }

        return fontDescent - fontAscent + lineSpace;
        }


    public void drawText( Canvas canvas )
        {
        int paraCounter = 0;
        int lineCounter = firstLine;

        while ( paraCounter < visibleParas.size() )
            {
            if ( paraCounter == visibleParas.size()-1 && lineCounter > lastLine ) // last paragraph
                break;

            visibleParas.get(paraCounter).getLine(lineCounter).draw( canvas, fontPaint );

            lineCounter++;
            if ( lineCounter > visibleParas.get(paraCounter).sizeOfLines())
                {
                paraCounter++;
                lineCounter=0;
                }
            }
        }
    }
