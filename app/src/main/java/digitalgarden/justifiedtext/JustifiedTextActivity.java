package digitalgarden.justifiedtext;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import digitalgarden.justifiedtext.description.VisibleText;

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

		justText.setVisibleText( new VisibleText(null), 0, 0 );
		}
		
	}
