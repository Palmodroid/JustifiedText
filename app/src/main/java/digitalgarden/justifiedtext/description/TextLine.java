package digitalgarden.justifiedtext.description;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.List;

/**
 * Descriptor of a line
 */
public class TextLine
    {
    // pointer to the words of the paragraph
    private List<TextWord> words;

    // firstWord == negative values: rendering was not started
    // lastWord < firstWord: no words in this line

    // first word of the line (among the words of the paragraph)
    public int firstWord = -1;
    // last word of the line (among the words of the paragraph)
    private int lastWord;

    // Height of the line
    private int lineType;

    public static final int LINE_EMPTY = 0;
    public static final int LINE_LAST = 1;
    public static final int LINE_NORMAL = 2;


    /**
     * Constructor
     * @param words list of all words of the paragraph
     */
    TextLine( List<TextWord> words )
        {
        this.words = words;
        }


    /**
     * Renders line: adds words from paragraph and justifies them
     * @param startWord start with this word (among the words of the paragraph)
     * @param spaceMin minimum space (in pixels) between words
     * @param spaceMax maximum space (in pixels) between words
     * @param width width available for the line
     * @return the No. of the first non-added word
     */
    public int render( int startWord, float spaceMin, float spaceMax, int width )
        {
        // iterator from startWord to the last word of the paragraph
        int wordCursor;
        // counter of words added inside the line
        int wordsInLine = 0;

        // width occupied by added words and minimal space between them
        float textWidth = 0f;

        // width of the remaining space inside the line
        float spaceWidth;
        // width of the current word
        float wordWidth;

        this.firstWord = startWord;
        this.lastWord = startWord - 1;

        // iterate through words
        for ( wordCursor = startWord; wordCursor < words.size(); wordCursor++ )
            {
            wordWidth = words.get(wordCursor).getWidth();

            // word can be added
            if ( textWidth + wordsInLine * spaceMin + wordWidth <= width )
                {
                lastWord++;
                wordsInLine++;
                textWidth += wordWidth;
                }
            // word is too long (cannot be added), but this is the first word in the line
            else if ( wordCursor == firstWord )
                {
                // long words will overflow width
                // this can be solved only if unit is smaller than a word
                return wordCursor + 1;
                }
            // word cannot be added, because there is no more space
            else
                {
                break;
                }
            }

        // This line is empty
        if ( wordsInLine == 0 )
            {
            lineType = LINE_EMPTY;
            return wordCursor; // It should be 0
            }

        // Last line - do not justify
        if ( wordCursor >= words.size() )
            {
            spaceWidth = spaceMin;
            lineType = LINE_LAST;
            }
        // Justify words
        else
            {
            spaceWidth = (width - textWidth - 0.1f) / wordsInLine;
            if (spaceWidth > spaceMax)
                spaceWidth = spaceMin;
            lineType = LINE_NORMAL;
            }

        // Set x position for each word inside line
        float positionX = 0;
        for (int word = startWord; word <= lastWord; word++)
            {
            words.get(word).setPosition(positionX);
            positionX += words.get(word).getWidth() + spaceWidth;
            }

        // return the first non-added word of the paragraph
        return wordCursor;
        }


    /**
     * Draws the words of this line onto the canvas
     * @param canvas canvas to draw on
     * @param positionY y position (in pixels)
     * @param paint paint to use for drawing
     * @return line type
     */
    public int draw(Canvas canvas, float positionY, Paint paint)
        {
        for ( int word = firstWord; word <= lastWord; word++ )
            {
            words.get(word).draw(canvas, positionY, paint);
            }

        return lineType;
        }
    }
