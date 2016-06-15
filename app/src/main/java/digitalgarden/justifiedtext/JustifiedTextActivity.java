package digitalgarden.justifiedtext;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;

import digitalgarden.justifiedtext.debug.Debug;
import digitalgarden.justifiedtext.description.TextDescriptor;
import digitalgarden.justifiedtext.scribe.Scribe;

public class JustifiedTextActivity extends Activity 
    {

    private EditText editText;
    private JustifiedTextView justifiedTextView;

    private TextDescriptor textDescriptor = null;


    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        Debug.initScribe( this );

        setContentView(R.layout.activity_justified_text);

        editText = (EditText) findViewById(R.id.editText);
        justifiedTextView = (JustifiedTextView) findViewById(R.id.justText);
        }

    @Override
    public void onResume()
        {
        super.onResume();

        try
            {
            textDescriptor = new TextDescriptor( "//proba.txt");
            justifiedTextView.setVisibleText(textDescriptor, 0L );
            }
        catch (FileNotFoundException e)
            {
            Scribe.error("COULD NOT FIND TEXT-DESCRIPTOR FILE!");
            }
        }

    @Override
    public void onPause()
        {
        super.onPause();

        if ( textDescriptor != null )
            try
                {
                textDescriptor.close();
                }
            catch (IOException e)
                {
                e.printStackTrace();
                }
        }
    }
