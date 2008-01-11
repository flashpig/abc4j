package abc.ui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import abc.notation.Note;

class JChordNote extends JNotePartOfGroup {

	public JChordNote(Note noteValue, Point2D base, ScoreMetrics c) {
		super(noteValue, base, c);
		//onBaseChanged();
	}
	
	protected void valuateNoteChars() {
		if (note.getStrictDuration()==Note.HALF || note.getStrictDuration()==Note.WHOLE) {
			noteChars = ScoreMetrics.NOTE_LONGER;
		}
		else
			noteChars = ScoreMetrics.NOTE;
	}
	
	public double render(Graphics2D context){
		//super.render(context);
		context.drawChars(noteChars, 0, 1, (int)displayPosition.getX(), (int)displayPosition.getY());
		renderExtendedStaffLines(context, m_metrics, m_base);
		renderAccidentals(context);
		renderDots(context);
		Color previousColor = context.getColor();
		context.setColor(Color.RED);
		context.drawLine((int)getStemX(), (int)getStemYBegin(), 
				(int)getStemX(), (int)getStemYBegin());
		context.setColor(previousColor);
		/*Color previousColor = context.getColor();
		context.setColor(Color.RED);
		context.drawLine((int)getStemBegin().getX(), (int)getStemBegin().getY(), 
				(int)getStemBegin().getX()+10, (int)getStemBegin().getY());
		context.setColor(previousColor);*/
		
		return m_width;
	}
}
