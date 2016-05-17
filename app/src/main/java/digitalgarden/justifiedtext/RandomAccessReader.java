package digitalgarden.justifiedtext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;

/*
 * Szekvenciális olvasással bármilyen kódolás betölthető:
 * 	// www.javamex.com/tutorials/io/character_stream_reader.shtml
 *	fis = new FileInputStream( file );				// byte alapú beolvasás
 *	isr = new InputStreamReader( fis, "UTF-8" );	// már karakteralapú, dekódolt és pufferelt (fix puffer)
 *	br = new BufferedReader( isr );					// readLine is van, és még nagyobbra állítható puffer
 *
 *	Van mód pozícionálásra:
 *	FileChannel fch = fis.getChannel();
 *	fch.position( pos );
 *	DE ELŐSZÖR A PUFFERT FOGJA KIOLVASNI!!
 *
 *	A puffer törlésére nincs metódus, ill. itt az InputStream-nél meglévő skip-available sem használható.
 *	A br.skip(Long.MAX_VALUE); a pufferelt változatoknál végigtölti a puffert a file-lal (végigolvassa)
 *
 *	Megoldás: akár saját pufferelt InputStreamReader, akár saját pufferelt RandomAccessFile elkészítése
 *	((A háttérben álló alacsony szintű funkciók ugyanazok))
 * 
 *	Dekódolás: A dekóder csak fix hosszúságú puffert byte-sort képes átalakítani, pl. egy egész sort -
 *	viszont ez univerzális.
 *	A saját UTF-8 dekóderünk folyamatos átalakítást végez 
 *	(addig olvassa a bemenetet, míg egy kimeneti karaktert össze nem rak)
 *	Ez utóbbi kettő együtt is implementálható.	
 *
 *	Pufferelt RAF példa:
 *	http://www.javaworld.com/article/2077523/build-ci-sdlc/java-tip-26--how-to-improve-java-s-i-o-performance.html
 */

// FileInputStream, InputStreamReader és RandomAccessFile elemeket is tartalmaz.
// Alapvetően azonban Reader leszármazott, legfeljebb FileReader-ből származtathatnánk.
public class RandomAccessReader extends Reader
	{
    private RandomAccessFile raf;
	private long markedPosition = 0L;		// Ez a file-ra vonatkozik!!

	private final int BUFFER_SIZE = 4096; // Nem lehet 0!, csak pufferből olvas!
											// A nagyobb érték a közeli ugrásoknál javíthat még valamit
	private byte[] buffer;

	private long bufferOffset;
	private int bufferLength;				// -1 érték "letiltja" a puffert, ez az EOF
	private int bufferPointer;

	private final int MAX_LINE_LENGTH = 8192; 	// Csak a biztonság kedvéért. Hátha túl nagy file-t olvasunk.
	/*
	 * raf:               bOffset    fPointer
	 * |-----------------|==========|--------------------|
	 *  0               9|1        1 2
	 *                   |0        9 0
	 * buffer:           |    bPointer
	 *                   |====X=====| bLength = 10;
	 *                    0   4    9
	 */                   
	
	
	// Konstruktorok
	public RandomAccessReader(File fil) throws IOException
		{
		super();
		raf = new RandomAccessFile( fil, "r");
		
		buffer = new byte[BUFFER_SIZE];
		invalidateBuffer(); // Ez valójában felesleges, mert minden paraméter 0 lesz, de illik inicializálni
		}

    public RandomAccessReader(String path) throws IOException 
		{
		this( new File(path) );
		}

    
	// Buffer műveletek
    
    // Érvénytelenítjük a puffert - a pozíció helyes lesz, de a puffer üres
    // VIGYÁZAT! Ez kikapcsolja az EOF jelzést!
	private void invalidateBuffer() throws IOException
		{
		bufferOffset = raf.getFilePointer(); 	// A buffer a "helyére" kerül, de üres
											 	// A tényleges feltöltést csak readByte() végzi majd el.
		bufferLength = 0;						// -1 érték EOF-t jelöl
		bufferPointer = 0;
		}
	
	// bufferLength == -1 -> EOF jelölésére (azaz puffer nem csak üres, hanem érvénytelen is)
    public boolean isEof()
    	{
    	return bufferLength == -1;
    	}

	// Egy byte beolvasása ill. a puffer feltöltése
	private int readByte() throws IOException
		{
		checkNotClosed();
		
		// Nincs adat a pufferben
		if ( bufferPointer >= bufferLength ) 
			{
			// EOF miatt tiltott
			if ( isEof() )
				return -1;

			// Tényleg elfogyott. Ha hamis, akkor invalidateBuffer() már meghívásra került pozícionáláskor
			if ( bufferLength > 0 )
				invalidateBuffer();
				
			// Feltöltjük a puffert
			// Elvileg visszatérési érték nem lehet 0 csak -1 vagy a beolvasott hossz, ezt azért ellenőrizzük
			bufferLength = raf.read(buffer, 0, BUFFER_SIZE);
			if ( bufferLength == 0 )
				throw new ArrayIndexOutOfBoundsException("ERROR!! raf.read(...) RETURNED 0!! IMPOSSIBILE!!");
			if ( isEof() )
		    	return -1;
		    }

		// Mivel javaban nincs unsigned, a byte->int konverzió miatt a "negatív" értéket törölni kell
        // Vagy: reader rész átalakítható negatív értékekre
		return buffer[bufferPointer++] & 0xFF;
		}
	
	// Ellenőrzések
	private void checkNotClosed() throws IOException
		{
		if ( raf==null )
			throw new IOException("RandomAccessReader already closed!");
		}

	
	// Bezárás
	@Override
	public void close() throws IOException 
		{
		checkNotClosed();
		raf.close();
		raf = null;
		buffer = null;
		
		bufferOffset = 0L;
		bufferLength = -1;
		bufferPointer = 0;
		}

	
	// Beolvasás - magasszintű, URF-8 kódolással - a beolvasás readByte()-on keresztül történik
	@Override
	public int read() throws IOException
		{
		// Első byte beolvasása
		int c0;
		do	{
			c0 = readByte();
			if ( c0 < 0x80 )		// 1 byte-os szekvencia: kész!
				{
				// if ( c0 == -1 )		eof = true;		// EOF jelzése más metódusoknak
				return c0;
				}
			} while (c0 < 0xC0);	// 80 és BF "követő" byte-okat átugorjuk, új byte-ot olvasunk 
	
		while (true)
			{
			// c0-ban itt az első, ún. vezérlőbyte van
			if (c0 < 0xC2 && c0 > 0xEF)
				{
				return '*';				// érvénytelen szekvencia! 
				}
	
			// Második byte beolvasása
			int c1 = readByte();
			if (c1 < 0x80 )
				{						// érvénytelen szekvencia!
				// if ( c0 == -1)			eof = true;			// EOF jelzése más metódusoknak
				return c1;				// Követő byte ASCII-t kódol, azt adjuk vissza
				}
			if (c1 > 0xBF)
				{						// érvénytelen szekvencia!
				c0 = c1;				// Követő byte első byte-nak felel meg, újrakezdjük
				continue;				
				}
			
			// 2 byte-os, érvényes szekvencia
			if (c0 < 0xE0) 
				// input 2 byte, output 5+6 = 11 bit
				return ((c0 & 0x1f) << 6) | (c1 & 0x3f);
		
			// Harmadik byte beolvasása
			int c2 = readByte();
			if (c2 < 0x80 )
				{						// érvénytelen szekvencia!
				// if ( c0 == -1 )			eof = true;		// EOF jelzése más metódusoknak
				return c2;				// Követő byte ASCII-t kódol, azt adjuk vissza
				}
			if (c2 > 0xBF)
				{						// érvénytelen szekvencia!
				c0 = c2;				// Követő byte első byte-nak felel meg, újrakezdjük
				continue;				
				}
			
			if (c0 == 0xE0 && c1 < 0xA0)
				{						// érvénytelen tartomány!
				return '*';
				}
			
			// 3 byte-os, érvényes szekvencia
			// input 3 byte, output 4+6+6 = 16 bit
			return ((c0 & 0x0f) << 12) | ((c1 & 0x3f) << 6) | (c2 & 0x3f);
			}
		}

	
	@Override
	public int read(char[] buf, int offset, int len) throws IOException
		{
        if ((offset | len) < 0 || offset > buf.length || buf.length - offset < len) 
        	{
        	throw new ArrayIndexOutOfBoundsException();
        	}
		
		int c;
		int l;
		
		for (l=0; l < len ; l++)
			{
			if ( (c=read()) == -1 )
				return -1;			// Jobb lenne break; akkor a hosszt adnánk vissza
			buf[offset+l] = (char)c;
			}
		
		return l;
		}

     
	public String readLine() throws IOException
		{
		return readLineTill( (char)0 ); 
		}
	
	
	public String readLineTill( char ending ) throws IOException
		{
		checkNotClosed();
		
		StringBuilder buf = new StringBuilder();
		
		int c;
		
		while ( (c = read()) >= ' ' && c != ending )
			{
			// Ez csak biztonsági ellenőrzés, nagyméretű, 0x0a szekvenciát nem tartalmazó file-ok miatt
			if ( buf.length() >= MAX_LINE_LENGTH )
				throw new IOException( "ERROR! readLine(): TextLine exceeds MAX_LINE_LENGTH!" );
			
			buf.append( (char)c );
			}
		return buf.toString(); 
		}

	
    // Pozícionálás
    @Override
    public boolean markSupported() 
		{
		return true;
    	}

    
    @Override
    public void mark(int readLimit) throws IOException 
		{
		markedPosition = bufferOffset + bufferPointer;
    	}


    @Override
    public void reset() throws IOException
		{
		seek( markedPosition );
		}

	
	//Pozícionálás RandomAccessFile módra
	public void seek( long newPosition ) throws IOException 
		{
		// newPosition a pufferben van
		long position = newPosition - bufferOffset;
		if ( position > 0L && position < (long)bufferLength)
			{
			bufferPointer = (int)position;
			}
		// tényleg tekernünk kell
		else
			{
			checkNotClosed();
			
			raf.seek( newPosition );
			invalidateBuffer();
			}
		}
	
	
    @Override
    public long skip( long skipBytes ) throws IOException 
		{
		if ( skipBytes <= 0 ) 
			return 0;

		// seek majd ellenőrzi, hogy pufferen belül vagyunk-e
		long tempPosition = getFilePointer();
		
		seek( tempPosition + skipBytes );
		
		return getFilePointer() - tempPosition;
		}

    
	public long getFilePointer() throws IOException
		{
		return bufferOffset + bufferPointer;
		}
	
	
	public long length() throws IOException
		{
		checkNotClosed();
		return raf.length();
		}
	
	}
	
