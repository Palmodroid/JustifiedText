
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
 * - before View is calculated (setFilePointer() can be called before)
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
    // At least one paragraph is needed after setPosition, otherwise the text is empty.
    private List<ParaDescriptor> visibleParas = new ArrayList<>();
    // first visible line of FIRST paragraph
    private int firstLine = -1;
    // last visible line of LAST paragraph
    private int lastLine = -1;

    // pointer right after the last paragraph
    private long lastFilePointer = -1L;


    private long firstLinePointer = -1;




    private void checkNotClosed() throws IOException
        {
        if (jigReader == null)
            throw new IOException("JigReader already closed!");
        }


    public TextDescriptor(String fileName ) throws FileNotFoundException
        {
        File file = new File( Environment.getExternalStorageDirectory(), fileName );
        this.jigReader =
            new JigReader( file );

        Scribe.debug("TextDescriptor file: [" + file.getAbsolutePath() + "] was opened.");

        setFontSize( 26f );
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
            Scribe.debug("Text descriptor is closed");
            }
        }


    public void setFontTypeface(Typeface typeface)
        {
        Scribe.debug("Font typeface is set: " + typeface );

        fontPaint.setTypeface( typeface );
        setFontDimensionData();
        }


    public void setFontSize( float textSize )
        {
        Scribe.debug("Font size is set: " + textSize );

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

        Scribe.debug("Font dimensions - ascent: " + fontAscent +
                ", descent: " + fontDescent +
                "; line space: " + lineSpace +
                ", last line space: " + lastLineSpace +
                ", empty line space: " + emptyLineSpace);
        }


    public void setFontColor( int color )
        {
        Scribe.debug("Font color is set: " + Integer.toHexString(color) );

        fontPaint.setColor( color );
        }


    public void setViewParameters( int viewWidth, int viewHeight, int viewMargin )
        {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.viewMargin = viewMargin;

        Scribe.debug("View parameters - width: " + viewWidth +
                ", height: " + viewHeight +
                ", margin: " + viewMargin);

        findFirstLine();
        }


    public void setFilePointer(long filePointer )
        {
        firstLinePointer = filePointer;
        visibleParas.clear(); // ??
        lastFilePointer = -1L; // ??
        firstLine = -1;
        lastLine = -1; // ??

        findFirstLine();
        }


    /**
     * Finds first line, if
     * - data is available
     * - first line was not found previously
     * Calls the builder after finding the right position.
     */
    public void findFirstLine()
        {
        if ( viewHeight < 0 )
            {
            Scribe.error("CANNOT FIND FIRST LINE - VIEW DATA IS STILL MISSING!");
            return;
            }

        if ( firstLinePointer < 0L )
            {
            Scribe.error("CANNOT FIND FIRST LINE - FILE POINTER IS STILL MISSING!");
            return;
            }

        if ( firstLine >= 0 )
            {
            Scribe.error("FIRST LINE WAS ALREADY FOUND!");
            return;
            }

        try
            {
            checkNotClosed();

            Scribe.debug("File pointer to set: " + firstLinePointer);

            // Find beginning of paragraph
            jigReader.seek(firstLinePointer);
            int c;
            while ((c = jigReader.readByteBackward()) != -1)
                {
                if (c == 0x0A)
                    {
                    jigReader.readByte(); // Skip 0x0A
                    break;
                    }
                }
            lastFilePointer = jigReader.getFilePointer();
            }
        catch ( IOException ioe )
            {
            Scribe.error("TEXT CANNOT BE READ BECAUSE OF I/O ERROR!");
            return;
            }

        Scribe.debug("File pointer of the selected paragraph: " + lastFilePointer);

        // Read first paragraph
        visibleParas.clear();
        readNextParagraph();

        // Find the first line
        int l = visibleParas.get(0).sizeOfLines() - 1; // at least 0
        while (l > 0)
            {
            if (firstLinePointer > visibleParas.get(0).getLine(l).getFilePointer())
                break;
            l--;
            }

        firstLine = l;

        Scribe.debug("Selected (first visible) line of the selected paragraph: " + firstLine);

        buildTextFromFirstLine();

        //pageDown();
        //lineDown();
        pageUp();
        pageUp();
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
        paragraph.renderLines( viewWidth, viewMargin );
        visibleParas.add( paragraph );

        Scribe.debug("Para added at: " + (visibleParas.size()-1) );
        paragraph.debug();

        return lastFilePointer;
        }


    /**
     * At least first paragraph with valid first line is needed.
     * A new text is created from the first line
     */
    private void buildTextFromFirstLine()
        {
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
                    if ( readNextParagraph() < 0L )
                        {
                        Scribe.error("END OF TEXT IS REACHED!");
                        break;
                        }
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


    public void pageDown()
        {
        int paraCounter = visibleParas.size() - 1;
        int lineCounter = lastLine;

        for ( int n = 0; n < 2; n++)
            {
            if (paraCounter > 0 || lineCounter > firstLine)
                {
                lineCounter--;
                if (lineCounter < 0)
                    {
                    paraCounter--;
                    lineCounter = visibleParas.get(paraCounter).sizeOfLines() - 1;
                    }
                }
            }

        firstLine = lineCounter;

        while ( paraCounter > 0 )
            {
            visibleParas.remove(0);
            paraCounter--;
            }

        buildTextFromFirstLine();
        }


    public void lineDown()
        {
        if ( visibleParas.size() > 1 || firstLine < lastLine )
            {
            firstLine++;
            if (firstLine >= visibleParas.get(0).sizeOfLines())
                {
                visibleParas.remove(0);
                firstLine = 0;
                }

            buildTextFromFirstLine();
            }
        }


    private long readPrevParagraph( )
        {
        long filePointer = visibleParas.get(0).getFilePointer();

        try
            {
            // Find beginning of paragraph
            jigReader.seek(filePointer);
            if ( jigReader.readByteBackward() == -1 )   // read 0x0a
                return -1L;

            int c;
            while ((c = jigReader.readByteBackward()) != -1)
                {
                if (c == 0x0A)
                    {
                    jigReader.readByte(); // Skip 0x0A
                    break;
                    }
                }

            filePointer = jigReader.getFilePointer();
            }
        catch ( IOException ioe )
            {
            return -1L; // Simulate BOF
            }

        ParaDescriptor paragraph = new ParaDescriptor();
        paragraph.readPara( jigReader, filePointer);
        paragraph.measureWords( fontPaint );
        paragraph.renderLines( viewWidth, viewMargin );
        visibleParas.add( 0, paragraph );

        Scribe.debug("Para added at: 0");
        paragraph.debug();

        return filePointer;
        }


    private void buildTextFromLastLine()
        {
        int paraCounter = visibleParas.size()-1;
        int lineCounter = lastLine + 1;

        // exact position cannot be calculated. Paras are read first, then rebuild from first line
        int positionY = viewMargin - fontAscent;

        while (positionY < viewHeight - viewMargin - fontDescent)
            {
            lineCounter--;
            if ( lineCounter < 0 )
                {
                paraCounter --;
                if ( paraCounter < 0 )
                    {
                    paraCounter = 0;
                    if ( readPrevParagraph() < 0L )
                        {
                        Scribe.error("BEGINNING OF TEXT IS REACHED!");

                        lineCounter = 0; // New first line is set
                        break;
                        }
                    }

                lineCounter = visibleParas.get(paraCounter).sizeOfLines()-1;
                }

            // this is not possible: visibleParas.get(paraCounter).getLine(lineCounter).setPositionY(positionY);
            positionY += getLineHeight(paraCounter, lineCounter);
            }

        firstLine = lineCounter;

        while ( paraCounter > 0 )
            {
            visibleParas.remove(0);
            paraCounter--;
            }

        // new build is needed if text is not long enough, and to position it
        buildTextFromFirstLine();
        }


    public void pageUp()
        {
        int paraCounter = 0;
        int lineCounter = firstLine;

        for ( int n = 0; n < 2; n++)
            {
            if (paraCounter < visibleParas.size()-1 || lineCounter < lastLine)
                {
                lineCounter++;
                if (lineCounter >= visibleParas.get(paraCounter).sizeOfLines())
                    {
                    paraCounter++;
                    lineCounter = 0;
                    }
                }
            }

        lastLine = lineCounter;

        paraCounter++;
        while ( paraCounter < visibleParas.size() )
            {
            visibleParas.remove(paraCounter);
            }

        buildTextFromLastLine();
        }


    public void lineUp()
        {
        if ( visibleParas.size() > 1 || firstLine < lastLine )
            {
            lastLine--;
            if (lastLine < 0)
                {
                visibleParas.remove(visibleParas.size()-1);
                lastLine = visibleParas.get(visibleParas.size()-1).sizeOfLines()-1;
                }

            buildTextFromLastLine();
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
        Scribe.locus();

        int paraCounter = 0;
        int lineCounter = firstLine;

        while ( paraCounter < visibleParas.size() )
            {
            Scribe.debug("PC: " + paraCounter + " / " + (visibleParas.size()-1));
            Scribe.debug("LC: " + lineCounter + " / " + (visibleParas.get(paraCounter).sizeOfLines()-1));

            if ( paraCounter == visibleParas.size()-1 && lineCounter > lastLine ) // last paragraph
                break;

            Scribe.debug( visibleParas.get(paraCounter).getLine(lineCounter).dump() );
            visibleParas.get(paraCounter).getLine(lineCounter).draw( canvas, fontPaint );

            lineCounter++;
            if ( lineCounter >= visibleParas.get(paraCounter).sizeOfLines())
                {
                paraCounter++;
                lineCounter=0;
                }
            }

        }
    }
