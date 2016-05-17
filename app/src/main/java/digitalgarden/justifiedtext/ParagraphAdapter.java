package digitalgarden.justifiedtext;

import android.os.Environment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ParagraphAdapter
	{
	static List<Long> positions;
	static {
		positions = new ArrayList<Long>();
		positions.add(new Long(0L));
		}
		
	public static int loadedParagraphsSize()
		{
		return positions.size();
		}
	
	public static Reader getParagraph(int paraNo) throws EOFException, FileNotFoundException, IOException
		{
		RandomAccessReader file = new RandomAccessReader(Environment.getExternalStorageDirectory().getAbsolutePath() + "/proba.txt");
		
		if ( paraNo >= positions.size() )
			{
			int c;
			
			file.seek(positions.get(positions.size()-1).longValue());
			while (positions.size() <= paraNo)
				{
				while ( (c=file.read()) != 0x0a) 
					{
					if (c == -1)
						{
						file.close();
						throw new EOFException();
						}
					}
				positions.add(Long.valueOf(file.getFilePointer()));
				}
			}
		
		file.seek(positions.get(paraNo));		
		return file;
		}
		
	public static void saveData(DataOutputStream file) throws IOException 
		{
		//file.writeInt( positions.size() );
		for (int n=0; n<positions.size(); n++)
			{
			file.writeLong( positions.get(n).longValue() );
			}
		}
			
	public static void loadData(DataInputStream file, int size) throws IOException
		{
		//int size = file.readInt();
		positions = new ArrayList<Long>( size );
		for (int n=0; n<size; n++)
			{
			positions.add( new Long( file.readLong() ) );
			}			
		}
		
	}
