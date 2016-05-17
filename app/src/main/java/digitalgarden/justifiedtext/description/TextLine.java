package digitalgarden.justifiedtext.description;

/**
 * Descriptor of a line
 */
public class TextLine
    {
    private float posy;

    private int firstWord;
    private int lastWord;

    private int wordCount;
    private int spaceCount;

    private float textWidth;
    private float spaceWidth;

    private boolean justified;

    // TextLine konstruktora
    TextLine()
        {
        firstWord = -1;
        lastWord = -2;

        wordCount = 0;
        spaceCount = -1;

        textWidth = 0f;
        spaceWidth = 0f;

        justified = true;
        }

    // Szó hozzáadása TextLine-hoz
    private boolean addWord(int wordNo)
        {
        float wordWidth = words.get(wordNo).width;

        // túl hosszú szavak nincsenek megfelelően lekezelve!
        // ezt csak részletes feldolgozásnál tudjuk megtenni
        if (wordWidth > getWidth() && firstWord == -1)
            {
            firstWord = wordNo;
            lastWord = wordNo;
            wordCount++;
            spaceCount++;
            textWidth += wordWidth;

            return true;
            }

        if ( textWidth + (spaceCount + 1) * spaceMin + wordWidth <= getWidth() )
            {
            if (firstWord == -1)
                {
                firstWord = wordNo;
                lastWord = wordNo;
                }
            else
                {
                lastWord++;
                }

            wordCount++;
            spaceCount++;
            textWidth += wordWidth;
            return true;
            }
        else
            return false;
        }
    }
