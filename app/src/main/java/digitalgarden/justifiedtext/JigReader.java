package digitalgarden.justifiedtext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by tamas on 2016.05.22..
 */
public class JigReader
    {
    /*
     * raf:               bOffset    fPointer
     * |-----------------|==========|--------------------|
     *  0               9|1        1 2
     *                   |0        9 0
     * buffer:           |    bPointer
     *                   |====X=====| bLength = 10;
     *                    0   4    9
     */

    private RandomAccessFile raf;

    private final int BUFFER_SIZE = 4096;

    private long bufferOffset;
    private byte[][] buffer = new byte[2][];
    private int[] bufferLength = new int[2];				// -1 érték "letiltja" a puffert, ez az EOF
    private int bufferActive = 0;
    private int bufferPointer = 0;



    // Konstruktorok
    public JigReader(File fil) throws IOException
        {
        super();
        raf = new RandomAccessFile( fil, "r");

        buffer[0] = new byte[BUFFER_SIZE];
        buffer[1] = new byte[BUFFER_SIZE];

        invalidateBuffer(); // Ez valójában felesleges, mert minden paraméter 0 lesz, de illik inicializálni
        }

    public JigReader(String path) throws IOException
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
        bufferLength[0] = 0;						// -1 érték EOF-t jelöl
        bufferLength[1] = 0;
        bufferActive = 0;
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
        if ( bufferPointer >= bufferLength[bufferActive] )
            {
            // EOF miatt tiltott
            if ( isEof() )
                return -1;

            if ( bufferActive == 0 )
                {
                bufferActive++;
                bufferPointer = 0;
                }




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
        return buffer[bufferActive][bufferPointer++] & 0xFF;
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









    }
