package digitalgarden.justifiedtext.description;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Descriptor of a word
 */
public class TextWord
    {
    // text of the word - between to (white)spaces
    private String text;

    // position of the word inside the text-file
    private long filePointer;

    // width of the word in pixels
    private float width;

    // x-position in pixels inside the line
    private float positionX;


    /**
     * Constructor - called by TextParagraph
     * @param text text of the word
     */
    TextWord( long filePointer, String text )
        {
        this.filePointer = filePointer;
        this.text = text;
        }


    /**
     * File pointer of the word
     */
    public long getFilePointer()
        {
        return filePointer;
        }


    /**
     * Measures word width - called by TextParagraph
     * http://stackoverflow.com/a/7579469 - difference between measureText() and getTextBounds()
     * @param paint paint used for measuring
     */
    public void measure( Paint paint )
        {
        this.width = paint.measureText( text );
        }

    /**
     * Returns previously measured (measure()) width - used by TextLine
     */
    public float getWidth()
        {
        return width;
        }


    /**
     * Sets x position inside line - called by TextLine
     * @param positionX
     */
    public void setPosition( float positionX )
        {
        this.positionX = positionX;
        }


    /**
     * Draws this word - called by TextLine
     * @param canvas canvas to draw on
     * @param positionY y position (in pixels)
     * @param paint paint to use for drawing
     */
    public void draw(Canvas canvas, float positionY, Paint paint)
        {
        canvas.drawText( text, positionX, positionY, paint);
        }
    }

