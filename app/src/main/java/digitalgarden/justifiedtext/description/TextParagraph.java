package digitalgarden.justifiedtext.description;

import android.graphics.Paint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import digitalgarden.justifiedtext.JigReader;
import digitalgarden.justifiedtext.scribe.Scribe;

/**
 * TextParagraph text between '\r' 0x0A-s.
 * Actually this means text between (including) current position and the next 0x0A
 * Reads all words from this paragraphs, measures them and then renders lines from the words.
 * After this point these "virtual" lines are used. Paragraph only provides its lines.
 */
public class TextParagraph
    {
    // Can be empty, but cannot be null!
    public List<TextWord> words;
    public List<TextLine> lines;

    private long filePosition;

    private float spaceMin;
    private float spaceMax;


    /**
     * Reads all words from the paragraph into a new word-list
     * @param jigReader jigReader to get text
     */
    public long readParagraph( JigReader jigReader, long fromPosition ) throws IOException
        {
        // words should be deleted, or this routine should come into the constructor
        words = new ArrayList<>();
        filePosition = fromPosition;

        jigReader.seek( filePosition );

        int chr;
        StringBuilder builder = new StringBuilder();

para:	while ( true )
            {
            // skip spaces (tabs and all chars bellow space are 'spaces')
            while ( (chr = jigReader.readWithoutException()) <= ' ' )
                {
                if ( chr == -1 || chr == 0x0a )
                    break para;
                }

            // words
            builder.setLength(0);    
            do
                {
                builder.append( (char)chr );
                chr = jigReader.readWithoutException();
                } while ( chr > ' ' );
            jigReader.unRead( chr );

            words.add( new TextWord( builder.toString() ) );
            }
        Scribe.debug("Para: " + words);

        return jigReader.getFilePointer();
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
