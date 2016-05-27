package digitalgarden.justifiedtext;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;

public class JustifiedTextActivity extends Activity 
    {

    private EditText editText;
    private JustifiedTextView justText;

    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justified_text);

        editText = (EditText) findViewById(R.id.editText);
        justText = (JustifiedTextView) findViewById(R.id.justText);

        // justText.setVisibleText( new VisibleText(null), 0, 0 );

        JigReader jig = null;

        try
            {
            jig = new JigReader(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/proba.txt");

            StringBuilder builder = new StringBuilder();

            int ch;

            builder.append("[");

            while ( (ch=jig.readByte()) != -1 )
                {
                builder.append( (char)ch );
                }

            builder.append("].[");

            while ( (ch=jig.readByteBackward()) != -1 )
                {
                builder.append( (char)ch );
                }

            builder.append("].[");

            jig.seek( 3L );
            builder.append( (char)jig.readByte() );
            jig.seek( 3L );
            builder.append( (char)jig.readByteBackward() );

            jig.seek( 8L );
            builder.append( (char)jig.readByte() );
            jig.seek( 8L );
            builder.append( (char)jig.readByteBackward() );

            builder.append("]");
            editText.setText( builder.toString() );

            jig.close();
            }
        catch (Exception e)
            {
            e.printStackTrace();
            }

        }

    }
