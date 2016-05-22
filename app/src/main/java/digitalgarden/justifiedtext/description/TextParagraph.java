package digitalgarden.justifiedtext.description;

import android.graphics.*;
import digitalgarden.justifiedtext.scribe.*;
import java.io.*;
import java.util.*;

/**
 * TextParagraph
 * Paragraph: from the current position to the next '\r' 0x0A
 */
public class TextParagraph
    {
    // Can be empty, but cannot be null!
    public List<TextWord> words;
    public List<TextLine> lines;

    private float spaceMin;
    private float spaceMax;

    private boolean resendData = false;
    private int readData = -1;
    
    private int read( Reader reader )
        {
        if (resendData)
            resendData = false;
        else
            {
            try
                {
                readData = reader.read();
                }
            catch (IOException e)
                {
                readData = -1;
                }
            }
        return readData;
        }

    private void unRead()
        {
        resendData = true;
        }

    /**
     * Reads all words from the paragraph into a new words list
     * @param reader reader to get text
     */
    public void readParagraph( Reader reader )
        {
        // words should be deleted, or this routine should come into the constructor
        words = new ArrayList<>();

        int chr;
        StringBuilder builder = new StringBuilder();

para:	while ( true )
            {
            // skip spaces (tabs and all chars bellow space are 'spaces')
            while ( (chr = read( reader )) <= ' ' )
                {
                if ( chr == -1 || chr == 0x0a )
                    break para;
                }

            // words
            builder.setLength(0);    
            do
                {
                builder.append( (char)chr );
                chr = read( reader );
                } while ( chr > ' ' );
            unRead();

            words.add( new TextWord( builder.toString() ) );
            }
        Scribe.debug("Para: " + words);

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
    public int renderLines( int width )
        {
        lines = new ArrayList<>();

        int wordCount = 0;
        while ( wordCount < words.size() )
            {
            TextLine line = new TextLine( words );
            wordCount = line.render( wordCount, spaceMin, spaceMax, width );
            lines.add( line );
            }

        return lines.size();
        }


    public int sizeOfLines()
        {
        return lines.size();
        }

    public TextLine getLine( int line )
        {
        return lines.get(line);
        }
    }
