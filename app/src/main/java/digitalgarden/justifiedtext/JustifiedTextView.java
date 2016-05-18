
package digitalgarden.justifiedtext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class JustifiedTextView extends View
	{	
	/******** Nem szövegspecifikus részek ********/

	// Konstruktorok - init()-et meghívják
	public JustifiedTextView(Context context)
		{
		super(context);
		init();
		}

	public JustifiedTextView(Context context, AttributeSet attrs)
		{
		super(context, attrs);
		init();
		}

	public JustifiedTextView(Context context, AttributeSet attrs, int defStyle)
		{
		super(context, attrs, defStyle);
		init();
		}
			

	// onMeasure - szöveggel kapcsolatos az onSizeChanged-ben
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
		{
//Toast.makeText(getContext(), "Measure()-ben vagyok", Toast.LENGTH_SHORT).show();

		setMeasuredDimension(measureIt(widthMeasureSpec), measureIt(heightMeasureSpec));
		}

	private int measureIt(int measureSpec) 
		{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) 
			{
			return specSize;
			} 
		else 
			{
			// UNSPECIFIED: kérjük a létező legnagyobbat!
			int result = Integer.MAX_VALUE;

			if (specMode == MeasureSpec.AT_MOST && result > specSize) 
				{	
				result = specSize;
				}

			return result;
			}
		}


	/******** Szövegspecifikus részek ********/

	// View egészére vonatkozó változók
	private Paint paintFont;
	private float fontAscent;
	private float fontDescent;
	private float fontLeading;

	private Paint paintBigFont;
	private Paint paintBigFontBorder;
	private Paint paintBigFontBackground;
	private Rect rectBigFont;
	private float bigFontAscent;
	private float bigFontDescent;
	
	private Paint paintBorder;
	private Rect border;

	private Paint paintAround;
	private Rect around;

	private List<Paragraph> paragraphs;
	
	// Képernyőfelosztásra vonatkozó változók
	private float touchX;
	private float touchY1;
	private float touchY2;

	
	// Viewra vonatkozo valtozok inicializalasa
	// Ide az kerul, amit csak egyszer kell kitolteni
	private void init()
		{
//Toast.makeText(getContext(), "Init()-ben vagyok", Toast.LENGTH_SHORT).show();
		
		paintFont = new Paint();
		paintFont.setColor(0xffffd4ab);
		paintFont.setTextSize(26f);

		fontAscent = paintFont.ascent();
		fontDescent = paintFont.descent();
		fontLeading = 5f;

		paintBigFont = new Paint();
		paintBigFont.setColor(Color.YELLOW);
		paintBigFont.setTextSize(50f);
		bigFontAscent = paintBigFont.ascent();
		bigFontDescent = paintBigFont.descent();

		paintBigFontBorder = new Paint();
		paintBigFontBorder.setStyle(Paint.Style.STROKE);
		paintBigFontBorder.setStrokeWidth(0f);
		paintBigFontBorder.setColor(Color.YELLOW);
		
		paintBigFontBackground = new Paint();
		paintBigFontBackground.setStyle(Paint.Style.FILL);
		paintBigFontBackground.setColor(0xff101010);
		rectBigFont = new Rect();
		
		paintBorder = new Paint();
		paintBorder.setStyle(Paint.Style.STROKE);
		paintBorder.setStrokeWidth(0f);
		paintBorder.setColor(Color.DKGRAY);
		border = new Rect();

		paintAround = new Paint();		
		paintAround.setStyle(Paint.Style.STROKE);
		paintAround.setStrokeWidth(0f);
		paintAround.setColor(Color.RED);
		around = new Rect();
		}
	//?? Innen hiányzik a tulajdonságok megváltozásakor lefutó rész

	
	public void setFontSize( float size)
		{
		paintFont.setTextSize( size );
		
		// ezeket at kell tenni vhova
		
		fontAscent = paintFont.ascent();
		fontDescent = paintFont.descent();
		fontLeading = 5f;			
		
		// a setposition torli a paragraph tombot
		// preparepara. csak kiegesziti!
		
		if (linesInView > 0)
			setPosition(getActualParagraphPosition(), getActualWordPosition());		
		// egyebkent a parameterek balfekseget adnak!
		}

	public float getFontSize()
		{
		return paintFont.getTextSize();
		}
	

	// Pozícionálásért felelõs részek
	// A kiirashoz itt elo kell kesziteni a bekezdeseket, ott mar csak a kiirassal kelljen torodni
	private int	linesInView = -1;

	// felesleges, mindig a 0. az elso // private int firstParagraphInView;
	private int firstLineInView;
	private int firstWordInView = -1;
	// amig nincs meret, addig ez jelzi. Utana -1 tiltja
	
	private int loadedLines;
	private int firstParagraph;
	
	private int selectedParagraph = -1;
	private int selectedWord = -1;

	
	// View méretváltozása 
	// Ide kerul minden, ami merettel kapcsolatos
	// A paragraphs.words marad, de a sorokat ujra kell szamolni
	// A words a betutipus valtozasnal valtozik meg (es akkor persze minden)
	
	// Meg egy gond: ez csak az onCreate utan all be!
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
		{
//Toast.makeText(getContext(), "onSizeChanged()-ben vagyok", Toast.LENGTH_SHORT).show();
		
		// View specifikus
		border.set(0, 0, w - 1, h - 1);

		setLinesInView();
		prepareParagraphs();
			
		touchX = 0.8f * (float)w;
		
		touchY1 = 0.333f * (float)h;
		touchY2 = 0.666f * (float)h;
		
		//setPosition(0);
		// Szovegspecifikus
		// invalidate itt nem kell, mert onDraw ugyis lesz
		}

	
	// Legegyszerűbb letiltani a képet, ha nullára áll a linesInView
	// Ezt majd a szövegbetöltés beállítja, ill.
	// ha nincs tiltva, onSizeChanged is állítja
	// Újabb változat:
	// linesInView = -1 -> az egész rendszer letiltva, nincs betöltött paragraphs! (Igazából paragraphAdapter sem)
	//				  0 -> a bekezdések betöltve, de nincs még méretünk, renderLines (prepareParagraph) nem futott le
	//				  + -> teljesen felállt a rendszer
	
	private void setLinesInView()
		{
		if ( linesInView >= 0 && getHeight() > 0f )
			linesInView = (int)(getHeight() / (-fontAscent + fontDescent + fontLeading)) + 1 ; // a töredéksort is el kell készíteni
		}
		
		
	public int getActualParagraphPosition()
		{
		return firstParagraph;
		}

	// Hibakezelés!!
	public int getActualWordPosition()
		{
		return paragraphs.get(0).lines.get(firstLineInView).firstWord;
		}

	
	public void setPosition(int firstPara, int firstWord)
		{
		paragraphs = new ArrayList<Paragraph>();
		//Toast.makeText(getContext(), " SETPOSITION beallitva ", Toast.LENGTH_SHORT).show();
			
		loadedLines = 0;

		firstParagraph = firstPara;		
		firstLineInView = 0;
		
		linesInView = 0; // Már nem tiltott
		setLinesInView(); // ez ugye eloszor tiltott, lehet h. csak 0 erteknel kellene beallitani??
		
		firstWordInView = firstWord; // beallitas, ha van meret!
		
		prepareParagraphs();		
		// egyebkent majd onSizeChanged beallitja		
		}

	
	public void rollForwardLine()
		{
		firstLineInView++;
		if (firstLineInView >= paragraphs.get(0).lines.size())
			{
			if (paragraphs.get(1).fake)
			// ha van legalább két sor, akkor ez be lesz töltve! De ha nincs?
			// prepare paragraphsba be lehet tenni egy biztonsági beolvasást, legalább 2 para legyen bent!
				{
				firstLineInView--;
				return;
				}
			loadedLines -= paragraphs.get(0).lines.size();
			firstParagraph++;
			selectedParagraph--;
			firstLineInView = 0;
			paragraphs.remove(0);
			}
		prepareParagraphs();
		}

	
	public void rollBackwardLine()
		{
		firstLineInView--;
		if (firstLineInView < 0)
			{
			firstParagraph--;
			if (firstParagraph < 0)
				{
				firstParagraph = 0;
				firstLineInView = 0;
				}
			else
				{
				Paragraph para = new Paragraph(firstParagraph);
				para.renderLines();
				paragraphs.add(0, para);
				loadedLines += para.lines.size();
				selectedParagraph++;
				firstLineInView = para.lines.size()-1;
				}
			}
		// prepareParagraphs(); ide nem ez kell, mert inkabb tobb van, mint keves
		while (paragraphs.size() > linesInView + 10)
			{
			loadedLines-= paragraphs.get(paragraphs.size()-1).lines.size();
			paragraphs.remove(paragraphs.size() -1);
			}
		// torolgeti az utolsokat
		// nem pontos puffert tart meg, hanem felteszi, h. minden para min. 1 sor. Es mwg ezen kivul megtart 10-et 
		}

	
	private void prepareParagraphs()
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
	
	
	// Szavak felrajzolása
	// Biztos, hogy a bekezdesek megvannak
	@Override
	protected void onDraw(Canvas canvas) 
		{
		canvas.drawRect(border, paintBorder);

		Paragraph.Line line;
		Paragraph.Word word;
		
		int paraPos = 0;
		int linePos = firstLineInView;
		
		String bigText = null;
		float bigy = 0;
		
		float posy = -fontAscent;
		
//Toast.makeText(getContext(), Integer.toString(linesInView) + " képsor", Toast.LENGTH_SHORT).show();
		
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
		
		// Ha van kiválasztott, felnagyítja
		if (bigText != null)
			{
			canvas.drawRect(rectBigFont, paintBigFontBackground);
			canvas.drawRect(rectBigFont, paintBigFontBorder);
			canvas.drawText(bigText, 10, (int)bigy, paintBigFont);
			}
			
		// Ha hozzáértünk a vezérlőkhöz, elengedésig kirajzolja a keretüket
		if (touchState == ROLL)
			{
			canvas.drawLine(touchX, 0, touchX, getHeight(), paintBorder);
			canvas.drawLine(touchX, touchY1, getWidth(), touchY1, paintBorder);
			canvas.drawLine(touchX, touchY2, getWidth(), touchY2, paintBorder);
			}

// EZ CSAK A PRÓBA RÉSZ, MIKÉNT LEHET IKONOKAT KIRAJZOLNI!
		
		// Elsőként elő kell állítani a felhasználható képet a resource-ból; hiba esetén értéke null, ezt le kellene kezelni
		Bitmap menuBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.menu);
		Bitmap csillagBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.csillagok);
		Bitmap drawingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.drawing);

		// csak felrajzolja
		// canvas.drawBitmap(mBitmap, 20, 20, null);
		
		// ez fel is nagyítja
		/*
		Paint paint = new Paint();
		paint.setFilterBitmap(true);
		// The image will be scaled so it will fill the width, and the
		// height will preserve the image’s aspect ration
		double aspectRatio = ((double) mBitmap.getWidth()) / mBitmap.getHeight();
		Rect dest = new Rect(0, 0, this.getWidth(),(int) (this.getHeight() / aspectRatio));
		canvas.drawBitmap(mBitmap, null, dest, paint);
		*/
		
		// Ez meg átszínezi
		/*
		 Paint paint = new Paint(Color.RED);
		 ColorFilter filter = new LightingColorFilter(Color.RED, 1);
		 paint.setColorFilter(filter);
		 canvas.drawBitmap(mBitmap, 20, 20, paint);
		 */
		
		// public void drawBitmap (Bitmap bitmap, Rect src (a képnek ezt a részét veszi, ha nem 0), RectF/Rect dst (erre a területre), Paint paint)
		// public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
		
		Paint paint = new Paint();
		ColorFilter filter = new LightingColorFilter(0xe58024, 0); //mul: ezzel szorozza, add: ezt meg hozzáadja. Tehát itt RED-en (és alfán, mivel red: 0xffff0000) kívül minden más 0 lesz.;; Nem, szerintem az alfával nem dolgozik
		paint.setColorFilter(filter);
		paint.setFilterBitmap(true);
		// The image will be scaled so it will fill the width, and the
		// height will preserve the image’s aspect ration
		// double aspectRatio = ((double) mBitmap.getWidth()) / mBitmap.getHeight();
		Rect dest = new Rect(0, 0, 130, 130);
		canvas.drawBitmap(menuBitmap, null, dest, paint);

		Rect dest1 = new Rect(130, 0, 260, 130);
		canvas.drawBitmap(csillagBitmap, null, dest1, paint);
		
		Rect dest2 = new Rect(260, 0, 390, 130);
		canvas.drawBitmap(drawingBitmap, null, dest2, paint);
		
		
		
		
		
		
		
		
		}


	/******** Érintésspecifikus részek ********/
	
	int	touchState = 0;
	int touchDirection = 0;
	TouchThread touchThread;
	
	final int ROLL = 1;
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) 
		{
		float x = event.getX();
		float y = event.getY();

		switch (event.getAction()) 
			{
		case MotionEvent.ACTION_DOWN:

			// Csak lehelyezésnél vált ROLL módba!
			if (x > touchX)
				{
				touchState = ROLL;
				invalidate();
				
if (y > touchY2)
	{
    // the local Thread used for count-down
	touchDirection = 1;
	touchThread = new TouchThread();
    touchThread.start();
	}
else if (y > touchY1)
	{
	}
else
	{
	// the local Thread used for count-down
	touchDirection = -1;
	touchThread = new TouchThread();
	touchThread.start();
	}
						
				break;
				}

		case MotionEvent.ACTION_UP:

if (touchThread != null)		
	touchThread.interrupt();

			// Ha ROLL-ban van (ott helyeztük le) ÉS ott emeljük fel -> görget
			if (x > touchX && touchState == ROLL)
				{
				touchState = 0;
				if (y > touchY2)
					{
					for (int n=2; n < linesInView; n++)
						rollForwardLine();
					}
				else if (y > touchY1)
					{
					if (onCommandListener != null)
						onCommandListener.onSelect();			
					}
				else
					{
					for (int n=2; n < linesInView; n++)
						rollBackwardLine();
					}
				invalidate();
				break;
				}
			
		case MotionEvent.ACTION_MOVE:

			// Ha kimentünk a széli részről: KILÉP A ROLL MÓDBÓL
			if (x < touchX && touchState == ROLL)
				{
				touchState = 0;
				invalidate();
				break;
				}
	
			// ROLL módban nincs kiválasztás!
			if (touchState == ROLL)
				break;
			
			
			// Bármi más történik: szókiválasztás: eredmény felemelésre!
			Paragraph.Line line;
			Paragraph.Word word;

			int paraPos = 0;
			int linePos = firstLineInView;
			
			selectedWord = -1;
			selectedParagraph = -1;

ready:
			for (int l=0; l < linesInView; l++)
				{
				line = paragraphs.get(paraPos).lines.get(linePos);
				if (y > line.posy + fontAscent && y < line.posy + fontDescent)
					{				
					for (int w=line.firstWord; w <= line.lastWord; w++)
						{
						word = paragraphs.get(paraPos).words.get(w);
						if (x > word.posx && x < word.posx + word.width)
							{
							selectedWord = w;
							selectedParagraph = paraPos;

//Toast.makeText(getContext(), paragraph.words.get(w).text + " (" + Integer.toString(w) + ".) kiválasztva!", Toast.LENGTH_SHORT).show();

							if (onWordSelectedListener != null && event.getAction()==MotionEvent.ACTION_UP)
								onWordSelectedListener.onSelect(word.text);
							break ready;
							}
						}
					}
		
				linePos++;
				if (linePos >= paragraphs.get(paraPos).lines.size())
					{
					paraPos++;
					linePos = 0;
					}
				}	
			this.invalidate();

			break;
			}
		return true;
		}
	
	
	/******* Saját listenerek *******/
	OnWordSelectedListener onWordSelectedListener = null;
	
    public void setOnWordSelectedListener(OnWordSelectedListener listener) 
    	{
        onWordSelectedListener = listener;    
    	}
    
    public interface OnWordSelectedListener 
    	{
    	public abstract void onSelect(String word);
		}
	
	OnCommandListener onCommandListener = null;

    public void setOnCommandListener(OnCommandListener listener) 
		{
        onCommandListener = listener;    
		}

    public interface OnCommandListener 
		{
    	public abstract void onSelect();
		}


private class TouchThread extends Thread
	{        	
    @Override
    public void run() 
    	{
        try 
			{		
			for (int cnt=0; cnt<3; cnt++)
				{
				sleep(100);
            	if (isInterrupted())
					{
					touchThread = null; // ez mehet ide??
            		return;
					}
				}
			
			int pause = 150;
			while(true) 
    			{
				for (int cnt=0; cnt<15; cnt++)
					{
     	    	   	sleep(pause);
            		if (isInterrupted())
         		   		{
						touchThread = null; // ez mehet ide??
      		      		return;
       		     		}    
					touchThreadHandler.sendEmptyMessage(0);
    				}
				if (pause > 85)
					pause -= 30;
				}
        	} 
        catch(Exception e) 
        	{
            // don't forget to deal with exceptions....
        	} 
        finally 
        	{
        	//this forces the activity to end (also the application in our case)
            //finish();
        	}
    	}
	};
    
private Handler touchThreadHandler = new Handler() 
	{
	@Override
	public void handleMessage(Message msg) 
		{
		// whenever the Thread notifies this handler we have 
		// only this behavior
		if (touchDirection >= 0)
			rollForwardLine();
		else
			rollBackwardLine();
		touchState = 0;
		invalidate();
		}
	};
    
    
	}
