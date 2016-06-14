package digitalgarden.justifiedtext.description;

import android.graphics.Paint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import digitalgarden.justifiedtext.jigreader.JigReader;
import digitalgarden.justifiedtext.scribe.Scribe;

/**
 * ParaDescriptor text between '\r' 0x0A-s.
 * Actually this means text between (including) current position and the next 0x0A
 * Reads all words from this paragraphs, measures them and then renders lines from the words.
 * After this point these "virtual" lines are used. Paragraph only provides its lines.
 */
public class ParaDescriptor
    {
    // Can be empty, but cannot be null!
    public List<WordDescriptor> words;
    public List<LineDescriptor> lines;

    private float spaceMin;
    private float spaceMax;


    /**
     * Reads all words from the paragraph into a new word-list
     * @param jigReader jigReader to get text
     * @return next file position, or -1L if EOF is reached
     */
    public long readPara(JigReader jigReader, long fromPosition )
        {
        // words should be deleted, or this routine should come into the constructor
        words = new ArrayList<>();

        try
            {
            jigReader.seek(fromPosition);
            }
        catch ( IOException ioe )
            {
            // Exception is thrown only if paragraph cannot be read because of i/o error
            // Empty paragraph is returned with EOF signal
            return -1L;
            }

        int chr;
        long wordPointer;
        StringBuilder builder = new StringBuilder();

para:	while ( true )
            {
            // skip spaces (tabs and all chars bellow space are 'spaces')
            while ( (chr = jigReader.readWithoutException()) <= ' ' )
                {
                if ( chr == -1 || chr == 0x0a )
                    break para;
                }

            // words - this could be inside wordText constructor
            try
                {
                wordPointer = jigReader.getFilePointer() - 1; // chr was read already
                }
            catch (IOException ioe )
                {
                // Exception is thrown only if paragraph cannot be read because of i/o error
                // Paragraph is finished here
                return -1L;
                }
            builder.setLength(0);
            do
                {
                builder.append( (char)chr );
                chr = jigReader.readWithoutException();
                } while ( chr > ' ' );
            jigReader.unRead( chr );

            words.add( new WordDescriptor( wordPointer, builder.toString() ) );
            }
        // Scribe.debug("Para: " + words);

        if ( chr == -1 ) // EOF is reached
            {
            return -1L;
            }

        try
            {
            return jigReader.getFilePointer();
            }
        catch (IOException ioe )
            {
            // Exception is thrown only if paragraph cannot be read because of i/o error
            // Paragraph is finished here
            return -1L;
            }
        }


    /**
     * All words are measured by paintFont
     * @param paintFont paint to use for measure
     */
    public void measureWords( Paint paintFont )
        {
        spaceMin = paintFont.measureText("www");
        spaceMax = paintFont.measureText("wwwww");

        for ( WordDescriptor word : words )
            {
            word.measure( paintFont );
            }
        }


    /**
     * Render lines for the specified width.
     * At least one line is generated (for empty paragraphs)
     * @param width width of the view
     */
    public int renderLines( int width )
        {
        lines = new ArrayList<>();

        int wordCount = 0;
        do  {
            LineDescriptor line = new LineDescriptor( words );
            wordCount = line.render( wordCount, spaceMin, spaceMax, width );
            lines.add( line );
            } while ( wordCount < words.size() );

        return lines.size();
        }


    public int sizeOfLines()
        {
        return lines.size();
        }

    public LineDescriptor getLine(int line )
        {
        return lines.get(line);
        }

    public void debug()
        {
        Scribe.debug("No. of lines: " + sizeOfLines());
        for ( LineDescriptor line : lines )
            {
            Scribe.debug(" - " + line.dump());
            }
        }
    }
