
package digitalgarden.justifiedtext.description;

import android.graphics.Canvas;
import android.graphics.Paint;
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
 * - before View is calculated (setPosition() can be called before)
 * - after View is calculated (setParameters() will be called after)
 */
public class VisibleText
    {
    private List<TextParagraph> visibleParagraphs = new ArrayList<>();
    private int loadedLines;
    private long lastPosition;

    // SOURCE PARAMETERS
    JigReader jigReader = null;

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
    private int width;
    private int height;
    private Paint fontPaint;


    public VisibleText( String fileName ) throws FileNotFoundException
        {
        File file = new File( Environment.getExternalStorageDirectory(), fileName );
        this.jigReader =
                new JigReader( file );
        }

    public void close() throws IOException
        {
        if ( jigReader != null )
            jigReader.close();
        }

    public void setPosition( int firstParagraph, int firstWord )
        {
        loadedLines = 0;
        lastPosition = 0L;
        visibleParagraphs.clear();

        this.firstParagraph = firstParagraph;
        this.firstLineInView = 0;
        this.firstWordInView = firstWord;

        this.linesInView = 100;

        prepareParagraphs();
        }


    public void setParameters( Paint fontPaint, int width, int height )
        {
        this.fontPaint = fontPaint;
        this.width = width;
        this.height = height;

        fontAscent = fontPaint.ascent();
        fontDescent = fontPaint.descent();
        fontLeading = 5f;

        this.lineHeight = -fontAscent + fontDescent + fontLeading;

        prepareParagraphs();
        }


    private void prepareParagraphs()
        {
        // both postion and view data are needed
        if ( linesInView < 0 || firstParagraph < 0 )
            return;

        TextParagraph paragraph;

        while ( loadedLines < linesInView + firstLineInView )
            {
            paragraph = new TextParagraph();
            try
                {
                lastPosition = paragraph.readParagraph( jigReader, lastPosition );

                paragraph.measureWords( fontPaint );
                loadedLines += paragraph.renderLines( width );
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
        // jobb lenne inkabb ket setPosition - egy ha nincs meret, egy, ha van
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

        while ( positionY < height )
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
