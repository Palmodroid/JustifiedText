package digitalgarden.justifiedtext.description;

import android.graphics.Paint;

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
    Reader reader;

    private List<TextParagraph> paragraphs;

    private int linesInView;
    private int width;
    private Paint fontPaint;

    private int firstParagraph;
    private int firstLineInView;

    private int loadedLines;


    public VisibleText( Reader reader )
        {
        this.reader = reader;
        }


    public void setPosition( int firstPara, int firstWord )
        {
        loadedLines = 0;

        firstParagraph = firstPara;
        firstLineInView = 0;

        linesInView = 0; // Már nem tiltott
        setLinesInView(); // ez ugye eloszor tiltott, lehet h. csak 0 erteknel kellene beallitani??

        firstWordInView = firstWord; // beallitas, ha van meret!

        prepareParagraphs();
        // egyebkent majd onSizeChanged beallitja
        }


    public void setParameters( Paint fontPaint, int linesInView, int width )
        {
        this.linesInView = linesInView;
        this.width = width;
        this.fontPaint = fontPaint;
        }


    public void prepareParagraphs()
        {
        // na, ezt alaposan meg kellene nezni, mirt lesz tortszam??
        if (linesInView <= 0)
            return;

        Paragraph para;

        while (loadedLines < linesInView + firstLineInView || paragraphs.size()<2)
            // két bekezdést mindig olvasson előre (egyébként ez csak akkor nem lehet meg, ha egyetlen soros a kép)
            // b. verzó: onMeasure nem engedi két sor alá, de ez nehéz, mert a betűméret is lehet óriási.
            {
            para = new Paragraph( firstParagraph + paragraphs.size() );
            // na és ha nincs elég bekezdés?
            if ( paragraphs.size() == 0 && para.fake)
                {
                firstParagraph = ParagraphAdapter.loadedParagraphsSize()-1;
                }
            // ez csak a legelejen tortenhet meg, minden 0, meg nincs betoltott paragraph
            else
                {
                para.renderLines();
                paragraphs.add(para);

                loadedLines += para.lines.size();
                }
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





    }
