package digitalgarden.justifiedtext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Jig reader
 */
public class JigReader
    {
    private RandomAccessFile raf;


    private class Block
        {
        final int SIZE = 4096;

        // offset of the first byte - start of the block
        long offset = 0;
        // data of file from offset
        byte[] data = new byte[SIZE];
        // length of data in bytes; -1 EOF is reached
        int length = 0;

        void readBlock(long offset) throws IOException
            {
            this.offset = offset;
            this.length = raf.read(this.data, 0, SIZE);
            }
        }


    private Block[] block = new Block[2];


    private int blockActive = 0;

    private int blockPointer = 0;


    public JigReader(String path) throws IOException
        {
        this(new File(path));
        }


    public JigReader(File fil) throws IOException
        {
        super();
        raf = new RandomAccessFile(fil, "r");
        block[0] = new Block();
        block[1] = new Block();
        }


    public int readByte() throws IOException
        {
        checkNotClosed();

        while (blockPointer >= block[blockActive].length)
            {
            if (blockActive == 0)
                {
                blockActive++;
                blockPointer = 0;
                }
            else // blockActive == 1
                {
                if (block[blockActive].length == -1)
                    return -1; // EOF

                block[0] = block[1];

                block[1] = new Block();
                block[1].readBlock(block[0].offset + block[0].length);
                // blockActive remains 1
                blockPointer = 0;
                }
            }

        return block[blockActive].data[blockPointer++] & 0xFF;
        }


    // Ellenőrzések
    private void checkNotClosed() throws IOException
        {
        if (raf == null)
            throw new IOException("RandomAccessReader already closed!");
        }


    public void close() throws IOException
        {
        checkNotClosed();

        try
            {
            raf.close();
            }
        finally
            {
            raf = null;
            block = null;
            }
        }

    public long getFilePointer() throws IOException
        {
        checkNotClosed();
        return block[blockActive].offset + blockPointer;
        }


    public long length() throws IOException
        {
        checkNotClosed();
        return raf.length();
        }
    }
