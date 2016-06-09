
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
        // At this point we are at the beginning of a paragraph, containing filePosition





        loadedLines = 0;
        lastPosition = 0L;
        visibleParagraphs.clear();

        this.firstParagraph = firstParagraph;
        this.firstLineInView = 0;
        this.firstWordInView = firstWord;

        this.linesInView = 100;

        prepareParagraphs();
        }





    private List<TextParagraph> visibleParagraphs = new ArrayList<>();
    private int loadedLines;
    private long lastPosition;

    /** -1, while no view parameters are available */
    private int firstParagraph = -1;
    private int firstLineInView;
    private int firstWordInView;

    private float fontAscent;
    private float fontDescent;
    private float fontLeading;

    private float lineHeight;

    // VIEW PARAMETERS
    /**
     * How many lines can be in the View?
     * -1, while no view parameters are available
     */
    private int	linesInView = -1;




    private void prepareParagraphs()
        {
        // both postion and view data are needed
        if ( linesInView < 0 || firstParagraph < 0 )
            return;

        TextParagraph paragraph;
        
        while ( heightOfText < viewHeight)
            {
            paragraph = new TextParagraph();
            try
                {
                lastPosition = paragraph.readParagraph( jigReader, lastPosition );

                paragraph.measureWords( fontPaint );
                loadedLines += paragraph.renderLines(viewWidth);
                visibleParagraphs.add( paragraph );

                if ( jigReader.isEof() )
                    break;
                }
            catch (IOException e)
                {
                return; // No more paragraphs are available
                }
                
                
                
                
            }
        
        
        

        while ( loadedLines < linesInView + firstLineInView )
            {
            paragraph = new TextParagraph();
            try
                {
                lastPosition = paragraph.readParagraph( jigReader, lastPosition );

                paragraph.measureWords( fontPaint );
                loadedLines += paragraph.renderLines(viewWidth);
                visibleParagraphs.add( paragraph );

                if ( jigReader.isEof() )
                    break;
                }
            catch (IOException e)
                {
                return; // No more paragraphs are available
                }

            }

        // Ez nem kell a ciklusba, mer csak egyszer kellhet meghivni
        // inkabb megegyszer meghivjuk onmagat, mert lehet,h. nincs eleg sor
        // jobb lenne inkabb ket setFilePosition - egy ha nincs meret, egy, ha van
        if (firstWordInView >= 0)
            {
            // Ez a rész állítja be a megfelelő szót
            // Mi a helyzet az üres sorokkal??
            // Ez a megoldás véd a túl nagy számoktól, mert olyankor beadja az utolsó sort.
            // Üres sorok esetén sincs gond: ott ugyan a 0. szó is "túl nagy", ezért az utolsó (0.) sort kapjuk meg
            firstLineInView = visibleParagraphs.get(0).lines.size()-1;
            while ( firstWordInView  < visibleParagraphs.get(0).lines.get(firstLineInView).firstWord )
                {
                firstLineInView--;
                }

            firstWordInView = -1;
            prepareParagraphs();
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
