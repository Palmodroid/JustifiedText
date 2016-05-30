package digitalgarden.justifiedtext;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;

import digitalgarden.justifiedtext.description.VisibleText;

public class JustifiedTextActivity extends Activity 
    {

    private EditText editText;
    private JustifiedTextView justText;

    private VisibleText visibleText = null;


    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_justified_text);

        editText = (EditText) findViewById(R.id.editText);
        justText = (JustifiedTextView) findViewById(R.id.justText);
        }

    @Override
    public void onResume()
        {
        super.onResume();
        try
            {
            visibleText = new VisibleText( "proba.txt");
            justText.setVisibleText( visibleText, 0, 0 );
            }
        catch (FileNotFoundException e)
            {
            e.printStackTrace();
            }
        }

    @Override
    public void onPause()
        {
        super.onPause();

        if ( visibleText != null )
            try
                {
                visibleText.close();
                }
            catch (IOException e)
                {
                e.printStackTrace();
                }
        }
    }
