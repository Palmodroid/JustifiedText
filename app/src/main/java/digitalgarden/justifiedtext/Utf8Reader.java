package digitalgarden.justifiedtext;

import java.io.*;

public class Utf8Reader extends Reader
	{
    private RandomAccessFile raf;
	private long markpos=0L;

	
    public Utf8Reader(String filename) throws FileNotFoundException 
		{
		super();
		raf = new RandomAccessFile( filename, "r");
		lock=raf;
		}
    
    
	public Utf8Reader(File fil) throws FileNotFoundException
		{
		super();
		raf = new RandomAccessFile( fil, "r");
		lock=raf;
		}
	
	/*
	 * RAF nem átvehető paraméterként, mert nem lehet belőle másikat csinálni. Akkor konkrétan azt a file-t fogjuk használni
	 */

	
	@Override
    public void close() throws IOException 
		{
		raf.close();
 		}

    
    @Override
    public void mark(int readLimit) throws IOException 
		{
		synchronized (lock) 
			{
			markpos = raf.getFilePointer();
			}
    	}

    
    @Override
    public boolean markSupported() 
		{
		return true;
    	}

    
    @Override
    public int read() throws IOException
		{
		synchronized (lock)
			{
			int c0 = raf.read();

			if (c0 < 0x80) 
				return c0; // input 1 byte, output 7 bit

			int c1 = raf.read();

			if (c1 == -1) 
				return -1; // partial EOF

			if (c0 < 0xe0) 
				// input 2 byte, output 5+6 = 11 bit
				return ((c0 & 0x1f) << 6) | (c1 & 0x3f);

			int c2 = raf.read();

			if (c2 == -1)
				return -1; // partial EOF

			// input 3 byte, output 4+6+6 = 16 bit
			return ((c0 & 0x0f) << 12) | ((c1 & 0x3f) << 6) | (c2 & 0x3f);
			}
   		}

    
    @Override
    public int read(char[] buf, int offset, int len)
		{
		synchronized (lock)
			{
			return -1;
			}
    	}

    
	String LoadLine() throws IOException
	{
	StringBuffer buf = new StringBuffer();
	int c;
	
	do {
		if ( (c = read()) == -1)
			break;
		buf.append((char)c);
		} while (c != 0x0a); 

	return buf.toString();
	}

	
    @Override
    public boolean ready() 
		{
		return true;
    	}

    
    @Override
    public void reset() throws IOException
		{
		synchronized (lock) 
			{
			raf.seek(markpos);
    		}
		}

    
    @Override
    public long skip(long ns) throws IOException 
		{
		synchronized (lock) 
			{
			if (ns <= 0) 
				return 0;
			long pos = raf.getFilePointer();
			
			raf.seek(pos + ns);
			
			return raf.getFilePointer() - pos;
			}
		}

    public long skipParagraph() throws IOException 
		{
		synchronized (lock) 
			{
			int c;
			while ((c=raf.read()) != 0x0a) 
				{
				if (c==-1)
					return -1L;
					// vagy itt dobhatna EOFException kivételt is
				}
			}
		return raf.getFilePointer();
		}
	
	
	public void seek(long pos) throws IOException 
		{
		raf.seek(pos);
		}
		
	public long getPosition() throws IOException
		{
		return raf.getFilePointer();
		}
	}
	
