package digitalgarden.justifiedtext.description;

import android.graphics.Paint;

/**
 * Descriptor of an individual words
 */
public class TextWord
    {
    private String text;
    private float width;
    private float positionX;

    TextWord( String text )
        {
        this.text = text;
        }

    public void measure( Paint paint )
        {
        this.width = paint.measureText( text );
        }

    public float getWidth()
        {
        return width;
        }

    public void setPosition( float positionX )
        {
        this.positionX = positionX;
        }
    }

