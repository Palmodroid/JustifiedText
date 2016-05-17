package digitalgarden.justifiedtext.description;

/**
 * Descriptor of an individual words
 */
public class TextWord
    {
    private String text;
    private float width;
    private float posx;

    // Szó konstruktora
    TextWord( String text, float width )
        {
        this.text = text;
        this.width = width;
        }
    }

