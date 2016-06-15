package digitalgarden.justifiedtext.description;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Descriptor of a word
 */
public class WordDescriptor
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
     * Constructor - called by ParaDescriptor
     * @param text text of the word
     */
    WordDescriptor(long filePointer, String text )
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
     * Measures word width - called by ParaDescriptor
     * http://stackoverflow.com/a/7579469 - difference between measureText() and getTextBounds()
     * @param paint paint used for measuring
     */
    public void measure( Paint paint )
        {
        this.width = paint.measureText( text );
        //Rect bounds = new Rect();
        //paint.getTextBounds( text, 0, text.length(), bounds);
        //this.width = bounds.width();
        }

    /**
     * Returns previously measured (measure()) width - used by LineDescriptor
     */
    public float getWidth()
        {
        return width;
        }


    /**
     * Sets x position inside line - called by LineDescriptor
     * @param positionX
     */
    public void setPosition( float positionX )
        {
        this.positionX = positionX;
        }


    /**
     * Draws this word - called by LineDescriptor
     * @param canvas canvas to draw on
     * @param positionY y position (in pixels)
     * @param paint paint to use for drawing
     */
    public void draw(Canvas canvas, float positionY, Paint paint)
        {
        canvas.drawText( text, positionX, positionY, paint);
        // canvas.drawLine( positionX, positionY-5f, positionX + width, positionY-5f, paint);
        }


    public String dump()
        {
        return " (" + filePointer + ") [" + text + "]";
        }
    }

