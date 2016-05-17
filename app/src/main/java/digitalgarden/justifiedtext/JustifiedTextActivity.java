package digitalgarden.justifiedtext;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import digitalgarden.justifiedtext.scribe.Scribe;

public class JustifiedTextActivity extends Activity 
	{

	private EditText editText;
	private JustifiedTextView justText;

	@Override
	public void onCreate(Bundle savedInstanceState) 
		{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_justified_text);

        Scribe.note( "Standard primary note" );
        Scribe.note_secondary( "Standard secondary note" );

        Scribe.note( 4, "Standard primary note" );
        Scribe.note( 5, "Standard secondary note" );




		editText = (EditText) findViewById(R.id.editText);
		justText = (JustifiedTextView) findViewById(R.id.justText);

		int paraPos = 0;
		int wordPos = 0;
		float fontSize = 26f;
		
		try
			{
			DataInputStream fileMainIn = new DataInputStream( openFileInput("main") );

			int size = fileMainIn.readInt();
			paraPos = fileMainIn.readInt();
			wordPos = fileMainIn.readInt();
			fontSize = fileMainIn.readFloat();
			fileMainIn.close();

			DataInputStream fileDataIn = new DataInputStream( openFileInput("data") );
			ParagraphAdapter.loadData(fileDataIn, size);
			fileDataIn.close();
			
			//Toast.makeText(this, "Betöltve: " + Integer.toString(size) + " bekezdés, " + Integer.toString(paraPos) + " / " + Integer.toString(wordPos) + " pozícióra.", Toast.LENGTH_SHORT).show();				
			}
		catch(Exception e)
			{
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
			}

		justText.setPosition(paraPos, wordPos);
		justText.setFontSize(fontSize);
			
		justText.setOnWordSelectedListener(new JustifiedTextView.OnWordSelectedListener()
			{
			public void onSelect(String word)
				{
				editText.setText(word);
				}
			});
			
		justText.setOnCommandListener(new JustifiedTextView.OnCommandListener()
			{
			public void onSelect()
				{
				try
					{
					//justText.setPosition( Integer.parseInt( editText.getText().toString()), 0 );
					justText.setFontSize( Float.parseFloat( editText.getText().toString() ));
					}
				catch(Exception e)
					{}
				}
			});
		}
		
	@Override
	protected void onPause()
		{
		super.onPause();
		
		/* Két file van:
		 * MAIN:
		 * int: size of positions
		 * int: actual paragraph position
		 * int: actual word position
		 * float: textSize
		 * DATA:
		 * long[size]: paragraph positions
		 */

		int size = 0;

		try
			{
			DataInputStream fileMainIn = new DataInputStream( openFileInput("main") );
			size = fileMainIn.readInt();
			fileMainIn.close();
			}
		catch(Exception e)
			{
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();				
			}
			
		try
			{
			DataOutputStream fileMainOut = new DataOutputStream( openFileOutput("main", 0) );
			fileMainOut.writeInt( ParagraphAdapter.loadedParagraphsSize() );
			fileMainOut.writeInt( justText.getActualParagraphPosition() );
			fileMainOut.writeInt( justText.getActualWordPosition() );
			fileMainOut.writeFloat( justText.getFontSize() );
			fileMainOut.close();
			
			if (size != ParagraphAdapter.loadedParagraphsSize())
				{
				DataOutputStream fileDataOut = new DataOutputStream( openFileOutput("data", 0) );
				ParagraphAdapter.saveData(fileDataOut);
				fileDataOut.close();
				
				//Toast.makeText(this, "Elmentve: " + Integer.toString(ParagraphAdapter.loadedParagraphsSize()) + " bekezdés.", Toast.LENGTH_SHORT).show();				
				}
			else
				{
				//Toast.makeText(this, "Csak alapadatok mentése szükséges.", Toast.LENGTH_SHORT).show();				
				}
			}
		catch(Exception e)
			{
			Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();				
			}
		}
	}
