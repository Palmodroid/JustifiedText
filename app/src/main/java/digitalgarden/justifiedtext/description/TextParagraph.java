package digitalgarden.justifiedtext.description;

import android.graphics.Paint;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;

/**
 * TextParagraph
 * Paragraph: from the current position to the next '\r' 0x0A
 */
public class TextParagraph
    {
    // Can be empty, but cannot be null!
    List<TextWord> words;
    List<TextLine> lines;

    private float spaceMin;
    private float spaceMax;

    private int read( PushbackReader reader )
        {
        try
            {
            return reader.read();
            }
        catch (IOException e)
            {
            return -1;
            }
        }


    /**
     * Reads all words from the paragraph into a new words list
     * @param reader reader to get text
     */
    public void readParagraph( PushbackReader reader )
        {
        // words should be deleted, or this routine should come into the constructor
        words = new ArrayList<>();

        int chr;
        StringBuilder builder = new StringBuilder();

para:	while ( true )
            {
            builder.setLength(0);

            // skip spaces (tabs and all chars bellow space are 'spaces')
            do
                {
                chr = read( reader );

                if ( chr == -1 || chr == 0x0A )
                    break para;
                } while ( chr <= ' ' );

            // words
            do
                {
                builder.append( (char)chr );
                chr = read( reader );
                } while ( chr > ' ' );

            words.add( new TextWord( builder.toString() ) );
            }
        }


    /**
     * All words are measured by paintFont
     * @param paintFont paint to use for measure
     */
    public void measureWords( Paint paintFont )
        {
        spaceMin = paintFont.measureText(".");
        spaceMax = paintFont.measureText("ww");

        for ( TextWord word : words )
            {
            word.measure( paintFont );
            }
        }


    /**
     * Render lines for the specified width
     * @param width width of the view
     */
    public void renderLines( int width )
        {
        lines = new ArrayList<>();

        int wordCount = 0;
        while ( wordCount < words.size() )
            {
            TextLine line = new TextLine( words );
            wordCount = line.render( wordCount, spaceMin, spaceMax, width );
            lines.add( line );
            }
        }
    }
