package digitalgarden.justifiedtext.description;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.List;

/**
 * Descriptor of a line
 */
public class TextLine
    {
    private List<TextWord> words;

    // negative values: rendering was not started
    public int firstWord = -1;
    private int lastWord;


    TextLine( List<TextWord> words )
        {
        this.words = words;
        }


    public int render( int first, float spaceMin, float spaceMax, int width )
        {
        int wordCursor;
        int wordsInLine = 0;

        float textWidth = 0f;
        float spaceWidth;

        float wordWidth;

        this.firstWord = first;
        this.lastWord = first - 1;

        // iterate through words
        for ( wordCursor = first; wordCursor < words.size(); wordCursor++ )
            {
            wordWidth = words.get(wordCursor).getWidth();

            if ( textWidth + wordsInLine * spaceMin + wordWidth <= width )
                {
                lastWord++;
                wordsInLine++;
                textWidth += wordWidth;
                }
            else if ( wordCursor == firstWord )
                {
                // long words will overflow width
                // this can be solved only if unit is smaller than a word
                return wordCursor + 1;
                }
            else
                {
                break;
                }
            }

        // This line is empty
        if ( wordsInLine == 0 )
            return wordCursor; // It should be 0

        if ( wordCursor >= words.size() ) // last line - do not justify
            {
            spaceWidth = spaceMin;
            }
        else
            {
            spaceWidth = (width - textWidth - 0.1f) / wordsInLine;
            if (spaceWidth > spaceMax)
                spaceWidth = spaceMin;
            }

        float positionX = 0;
        for (int word = first; word <= lastWord; word++)
            {
            words.get(word).setPosition(positionX);
            positionX += words.get(word).getWidth() + spaceWidth;
            }

        return wordCursor;
        }

    public void draw(Canvas canvas, float positionY, Paint paint)
        {
        for ( int word = firstWord; word <= lastWord; word++ )
            {
            words.get(word).draw(canvas, positionY, paint);
            }
        }
    }
