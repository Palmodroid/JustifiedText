package digitalgarden.justifiedtext.description;

import android.graphics.Paint;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;

/**
 * TextParagraph
 * Paragraph: from the current position to the next '\r' or '\n'
 * (\n after \r and \r after \n is consumed, too)
 */
public class TextParagraph
    {
    List<TextWord> words;
    List<TextLine> lines;
    boolean fake;

    // Feldolgozza a bekezdés összes szavát, még sortörés nélkül
    TextParagraph(int paraNo)
        {
        }

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

    private void unread( PushbackReader reader, int chr )
        {
        try
            {
            reader.unread( chr );
            }
        catch (IOException e)
            {
            ; // nothing to do, if io error happens
            }
        }

    /**
     * Reads all words from the paragraph into a new words list
     * @param reader reader to get text
     */
    public void readParagraph( PushbackReader reader, Paint paintFont )
        {
        words = new ArrayList<>();
        int chr;
        StringBuilder builder = new StringBuilder();

para:	while ( true )
            {
            // skip spaces (tabs and all chars bellow space are 'spaces')
            do
                {
                chr = read( reader );

                if ( chr == -1 )
                    break para;
                if ( chr == '\r' )
                    {
                    if ( (chr = read(reader)) != '\n' )
                        unread( reader, chr );
                    break para;
                    }
                if ( chr == '\n' )
                    {
                    if ( (chr = read(reader)) != '\r' )
                        unread( reader, chr );
                    break para;
                    }
                } while ( chr <= ' ' );

            // words
            builder.setLength(0);
            do
                {
                builder.append( (char)chr );
                chr = read( reader );
                } while ( chr > ' ' );

            String string=builder.toString();
            TextWord w=new TextWord(string, paintFont.measureText(string));
            words.add(w);
            }
        }

    // Bekezdés szavainak alapján elkészíti a bekezdés sorait.
    void renderLines()
        {
//Toast.makeText(getContext(), "RenderLines()-ben vagyok", Toast.LENGTH_SHORT).show();

        lines = new ArrayList<Line>();

        Line line = null;
        int wordCount = 0;
        boolean ok = false;

        while (!ok)
            {
            line = new Line();
            lines.add(line);
            ok = true;

            // szavak hozzáadása, amíg befér
            while ( wordCount < words.size() )
                {
                ok = line.addWord(wordCount );

                if (ok)
                    wordCount++;
                else
                    break;
                }

            // tényleges pozíció számítása
            if (line.spaceCount>0 && !ok && line.justified)
                {
                line.spaceWidth = (getWidth() - line.textWidth - 0.1f) / line.spaceCount;
                if (line.spaceWidth > spaceMax)
                    line.spaceWidth = spaceMin;
                }
            else
                line.spaceWidth = spaceMin;

            float posx = 0;
            for (int w=line.firstWord; w <= line.lastWord; w++)
                {
                words.get(w).posx = posx;
                posx += words.get(w).width + line.spaceWidth;
                }

            }

//Toast.makeText(getContext(), Integer.toString(lines.size()) + " sor kész", Toast.LENGTH_SHORT).show();
        }
    }
