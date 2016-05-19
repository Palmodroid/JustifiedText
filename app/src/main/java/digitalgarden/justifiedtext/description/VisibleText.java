package digitalgarden.justifiedtext.description;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import digitalgarden.justifiedtext.ParagraphAdapter;

/**
 * Visible parts of the text
 * This can be started:
 * - before View is calculated (setPosition() can be called before)
 * - after View is calculated (setParameters() will be called after)
 */
public class VisibleText
    {
    private List<TextParagraph> paragraphs = new ArrayList<>();
    private int loadedLines;

    // VIEW PARAMETERS
    /**
     * How many lines can be in the View?
     * -1, while no view parameters are available
     */
    private int	linesInView = -1;
    private int width;
    private Paint fontPaint;

    // SOURCE PARAMETERS
    Reader reader;
    /** -1, while no view parameters are available */
    private int firstParagraph = -1;
    private int firstLineInView;
    private int firstWordInView;



    public VisibleText( Reader reader )
        {
        this.reader = reader;
        }


    public void setPosition( int firstParagraph, int firstWord )
        {
        loadedLines = 0;
        paragraphs.clear();

        this.firstParagraph = firstParagraph;
        this.firstLineInView = 0;
        this.firstWordInView = firstWord;

        prepareParagraphs();
        }


    public void setParameters( Paint fontPaint, int width, int height )
        {
        this.fontPaint = fontPaint;
        this.width = width;
        this.linesInView = countLinesInView( height );

        prepareParagraphs();
        }


    private int countLinesInView( int height )
        {
        float fontAscent = fontPaint.ascent();
        float fontDescent = fontPaint.descent();
        float fontLeading = 5f;

        return (int)(height / (-fontAscent + fontDescent + fontLeading)) + 1;
        // the last 'broken' line is needed, too
        }


    public void prepareParagraphs()
        {
        // both postion and view data are needed
        if ( linesInView < 0 || firstParagraph < 0 )
            return;

        TextParagraph paragraph;

        while ( loadedLines < linesInView + firstLineInView )
            {
            paragraph = new TextParagraph();
            try
                {
                paragraph.readParagraph( ParagraphAdapter.getParagraph( firstParagraph + paragraphs.size() ) );
                }
            catch (IOException e)
                {
                return; // No more paragraphs are available
                }

            paragraph.measureWords( fontPaint );
            loadedLines += paragraph.renderLines( width );
            paragraphs.add( paragraph );
            }

        // Ez nem kell a ciklusba, mer csak egyszer kellhet meghivni
        // inkabb megegyszer meghivjuk onmagat, mert lehet,h. nincs eleg sor
        // jobb lenne inkabb ket setPosition - egy ha nincs meret, egy, ha van
        if (firstWordInView >= 0)
            {
            // Ez a rész állítja be a megfelelő szót
            // Mi a helyzet az üres sorokkal??
            // Ez a megoldás véd a túl nagy számoktól, mert olyankor beadja az utolsó sort.
            // Üres sorok esetén sincs gond: ott ugyan a 0. szó is "túl nagy", ezért az utolsó (0.) sort kapjuk meg
            firstLineInView = paragraphs.get(0).lines.size()-1;
            while ( firstWordInView  < paragraphs.get(0).lines.get(firstLineInView).firstWord )
                {
                firstLineInView--;
                }

            firstWordInView = -1;
            prepareParagraphs();
            }
        }

    public void drawText( Canvas canvas )
        {
        int paraPos = 0;
        int linePos = firstLineInView;

        float posy = -fontAscent;

        for (int l=0; l < linesInView; l++)
            {
            line = paragraphs.get(paraPos).lines.get(linePos);
            line.posy = posy;
            for (int w=line.firstWord; w <= line.lastWord; w++)
                {
                word = paragraphs.get(paraPos).words.get(w);

                if (w == selectedWord && paraPos == selectedParagraph)
                    {
                    paintAround.setColor(Color.YELLOW);
                    around.set((int)word.posx, (int)(posy + fontAscent), (int)(word.posx + word.width), (int)(posy + fontDescent));
                    canvas.drawRect(around, paintAround);

                    paintFont.setColor( Color.YELLOW );

                    // Nagyítás
                    bigText = word.text;
                    if (posy < getHeight()/4)
                        bigy = getHeight() - 10f - bigFontDescent;
                    else
                        bigy = 10f - bigFontAscent;
                    rectBigFont.set(10, (int)(bigy + bigFontAscent), (int) (10 + paintBigFont.measureText(bigText)), (int)(bigy + bigFontDescent));
//Toast.makeText(getContext(), rectBigFont.toString(), Toast.LENGTH_SHORT).show();

                    }
                else
                    paintFont.setColor(0xffffd4ab);

                canvas.drawText(word.text, word.posx, posy, paintFont);
                }
            posy+= -fontAscent + fontDescent + fontLeading;

            linePos++;
            if (linePos >= paragraphs.get(paraPos).lines.size())
                {
                paraPos++;
                linePos = 0;
                }
            }
        }
    }
