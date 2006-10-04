package abc.ui.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.*;
import scanner.*;
import abc.notation.*;
import abc.parser.*;
import java.util.*;

/** A pane for displaying and editing tunes. This pane handles copy/paste
 * actions. */
public class TuneEditorPane extends JTextPane implements ActionListener
{
  private static Color BACKGROUND_COLOR = new Color(249,234,202);
  private static Color FIELDS_COLOR = new Color (0,128,0);
  private static Color GRACING_FOREGROUND_COLOR = Color.green;
  private static Color GRACING_BACKGROUND_COLOR = FIELDS_COLOR;
  private static Color TUPLET_FOREGROUND_COLOR = new Color(128,0,255);
  private static Color SELECTION_FOREGROUND_COLOR = Color.white;//new Color(10,36,106);
  private static Color SELECTION_BACKGROUND_COLOR = new Color(10,36,106);
  private static Color BAR_COLOR = new Color (0,0,128);
  private static final String COPY_ACTION = "Copy";
  private static final String PASTE_ACTION = "Paste";
  public static final String ERROR_STYLE = "error";
  public static final String BARS_STYLE = "bars";
  public static final String TEXT_STYLE = "text";
  public static final String COMMENT_STYLE = "comment";
  public static final String NOTE_STYLE = "note";
  public static final String GRACING_STYLE = "note";
  public static final String FIELD_STYLE = "field";
  public static final String RHYTHM_STYLE = "rhythm";
  public static final String DEFAULT_STYLE = "rhythm";
  public static final String REFRESHER_THREAD_NAME = "ABC-TunePaneRefresh";

  //private static final boolean ENABLE_COLORING = true;

  //private int nbApply =0;
  private boolean m_forceRefresh = false;
  /** */
  private Style m_barStyle, m_textStyle, m_errorStyle,m_fieldStyle, m_rhythmStyle,
  	m_defaultStyle, m_baseNoteStyle, m_commentStyle, m_gracingStyle = null;
  private static final int IDLE_TIME_BEFORE_REFRESH = 200;
  /** The thread in charge of refreshing the tune representation of this editor pane. */
  private ParsingRefresh m_refresher = null;
  /** The tune currently represented in this editor pane. */
  private Tune m_tune = null;
  private TuneParser m_tuneParser = null;
  private int m_idleTimeBeforeRefresh = IDLE_TIME_BEFORE_REFRESH;
  private boolean m_enableColoring = false;

  private EditorKit m_editorKit = null;

  public TuneEditorPane()
  { this(new TuneParser()); }

  public TuneEditorPane(TuneParser parser)
  { this(parser, IDLE_TIME_BEFORE_REFRESH); }

  public TuneEditorPane(int idleTimeBeforeRefresh)
  { this(new TuneParser(), idleTimeBeforeRefresh); }

  public TuneEditorPane(TuneParser parser, int idleTimeBeforeRefresh)
  {
    setBackground(BACKGROUND_COLOR);
    setSelectedTextColor(SELECTION_FOREGROUND_COLOR);
    setSelectionColor(SELECTION_BACKGROUND_COLOR);
    setFont(new Font("Courier", Font.PLAIN, 12));
    m_tuneParser = parser;
    m_refresher = new ParsingRefresh((DefaultStyledDocument)getDocument(), parser);
    KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
    // Identifying the copy KeyStroke user can modify this
    // to copy on some other Key combination.
    KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
    // Identifying the Paste KeyStroke user can modify this
    //to copy on some other Key combination.
    registerKeyboardAction(this,COPY_ACTION,copy,JComponent.WHEN_FOCUSED);
    registerKeyboardAction(this,PASTE_ACTION,paste,JComponent.WHEN_FOCUSED);
    createStyles();
    setCharacterAttributes(m_defaultStyle, true);
  }

  public boolean getScrollableTracksViewportWidth()
  { return false;  }

  public TuneParser getParser()
  { return m_refresher.getParser(); }

  public boolean isColoringEnabled()
  { return m_enableColoring; }

  public void setColoringEnable(boolean coloring)
  {
    m_enableColoring = coloring;
    if (m_enableColoring)
      m_refresher.redrawTune();
    else
    {
        ((DefaultStyledDocument)getDocument()).setCharacterAttributes(
    0,getDocument().getEndPosition().getOffset(),m_defaultStyle, true);
      //setSelectedTextColor(SELECTION_FOREGROUND_COLOR);
      //setSelectionColor(SELECTION_BACKGROUND_COLOR);
    }
  }

  public void setSize(Dimension d)
  {
    if(d.width < getParent().getSize().width)
      d.width = getParent().getSize().width;
    super.setSize(d);
  }

  public Tune getTune()
  { return m_tune; }

  public void setDocument(Document doc)
  {
    //System.out.println("TuneEditorPane - setDocument(" + doc  + ")");
    //m_forceRefresh = true;
    super.setDocument(doc);
    /*if (m_refresher!=null)
    {
      setCharacterAttributes(m_defaultStyle, true);
      m_refresher.setDocument((DefaultStyledDocument)doc);
    }*/
  }

  public void setText(String text)
  {
/*  	try
  	{

    //System.out.println("TuneEditorPane - setText(" + text  + ")");
    DefaultStyledDocument doc = new DefaultStyledDocument();
    doc.insertString(0,text,m_defaultStyle);
    m_refresher.setDocument(doc);
    this.setDocument(doc);
    }
      catch (Exception e)
      { e.printStackTrace();
      }*/

    try
    {
    	m_refresher.stopIt();
       	getDocument().remove(0,getDocument().getLength()-1);
    	getDocument().insertString(0,text,m_defaultStyle);
    	m_refresher.startIt();
    }
    catch (Exception e)
    {e.printStackTrace();
    }
    //super.setText(text);
    //m_tuneParser.parse(text);
  }

  private void createStyles()
  {
    m_defaultStyle = addStyle(DEFAULT_STYLE, null);
    StyleConstants.setFontFamily(m_defaultStyle, "Courier");
    m_errorStyle = addStyle(ERROR_STYLE, m_defaultStyle);
    StyleConstants.setBackground(m_errorStyle, Color.red);
    StyleConstants.setForeground(m_errorStyle, Color.yellow);
    StyleConstants.setBold(m_errorStyle, true);
    m_baseNoteStyle = addStyle(NOTE_STYLE, m_defaultStyle);
    StyleConstants.setForeground(m_baseNoteStyle, Color.red);
    m_gracingStyle = addStyle(GRACING_STYLE, m_defaultStyle);
    StyleConstants.setBackground(m_gracingStyle, GRACING_BACKGROUND_COLOR);
    StyleConstants.setForeground(m_gracingStyle, GRACING_FOREGROUND_COLOR);
    m_textStyle = addStyle(TEXT_STYLE, m_defaultStyle);
    StyleConstants.setForeground(m_textStyle, Color.blue);
    m_commentStyle = addStyle(COMMENT_STYLE, m_defaultStyle);
    StyleConstants.setForeground(m_commentStyle, Color.white);
    StyleConstants.setBackground(m_commentStyle, Color.gray);
    m_barStyle = addStyle(BARS_STYLE, m_defaultStyle);
    StyleConstants.setForeground(m_barStyle, BAR_COLOR);
    StyleConstants.setBold(m_barStyle, true);
    m_fieldStyle = addStyle(FIELD_STYLE, m_defaultStyle);
    StyleConstants.setForeground(m_fieldStyle, FIELDS_COLOR);
    StyleConstants.setBold(m_fieldStyle, true);
    StyleConstants.setBold(m_fieldStyle, true);
    m_rhythmStyle = addStyle(RHYTHM_STYLE, m_defaultStyle);
    StyleConstants.setForeground(m_rhythmStyle, TUPLET_FOREGROUND_COLOR);
    StyleConstants.setBold(m_rhythmStyle, true);
  }

  /** This method is activated on the Keystrokes we are listening to
   * in this implementation. Here it listens for Copy and Paste ActionCommands. */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().compareTo(COPY_ACTION)==0)
    {
      StringBuffer sbf=new StringBuffer();
      //System.out.println("COPY ! : " + getSelectedText());
      StringSelection stsel  = new StringSelection(getSelectedText());
      Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
      system.setContents(stsel,stsel);
    }
    if (e.getActionCommand().compareTo(PASTE_ACTION)==0)
    {
      Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
      try
      {
        String selectedText = getSelectedText();
        String trstring= (String)(system.getContents(this).getTransferData(DataFlavor.stringFlavor));
        //System.out.println("PASTE ! : " + trstring);
        if (selectedText!=null)
          getDocument().remove(getCaretPosition(), selectedText.length());
        getDocument().insertString(getCaretPosition(), trstring, m_defaultStyle);
      }
      catch(Exception ex)
      {ex.printStackTrace();}
    }
  }

  private class ParsingRefresh extends Thread implements DocumentListener, TuneParserListenerInterface
  {
    private int m_elapsedTimeSinceLastParsing = 0;
    private DefaultStyledDocument m_document = null;
    private TuneParser m_parser = null;
    private int m_idleTime = 0;
    private Object m_mutex = new Object();
    private Vector m_parsingEvents = null;
    private TokenType m_contextForText = null;
    private TokenType m_contextForNote = null;

    public ParsingRefresh(DefaultStyledDocument document, TuneParser parser)
    {
      super(REFRESHER_THREAD_NAME);
      start();
      m_parser = parser;
      m_parsingEvents = new Vector();
      m_parser.addListener(this);
      m_document = document;
      startIt();
      //m_document.addDocumentListener(this);
    }

    public void startIt()
    {
    	m_document.addDocumentListener(this);
    	m_forceRefresh=true;
    	synchronized(m_mutex)
      	{
        	m_mutex.notify();
      	}

    }

    public void stopIt()
    {
    	m_document.removeDocumentListener(this);
    }

    public TuneParser getParser()
    { return m_parser; }

    public void setDocument(DefaultStyledDocument doc)
    {
    	try
    	{
      System.out.println(this.getClass().getName() + " - setDocument(" + doc + ")");
      m_document.removeDocumentListener(this);
      m_document = doc;
      m_tune = m_parser.parse(doc.getText(0, doc.getLength()));
      m_document.addDocumentListener(this);
      }
      catch (Exception e)
      { e.printStackTrace();
      }
    }

    public void run()
    {
      while (true)
      {
        try
        {
          synchronized(m_mutex)
          {
            //System.out.println("Area - wait()");
            m_mutex.wait();
            if (!m_forceRefresh)
            {
              do
              {
                //System.out.println("Area - compting before parsing : " + m_idleTime);
                m_mutex.wait(10);
                m_idleTime+=10;
              }
              while (m_idleTime<=IDLE_TIME_BEFORE_REFRESH);
            }
            try
            {

            	String tuneNotation = TuneEditorPane.this.getDocument().getText(0, TuneEditorPane.this.getDocument().getLength());
            	if (!tuneNotation.equals(""))
            	{
            		if (m_forceRefresh==true)
	            	{
    	          		m_forceRefresh = false;
        	      		//System.out.println("Area - Forcing refresh");
            		}
            		//System.out.println("Area - parsing("+ tuneNotation +")");
              		m_tune = m_parser.parse(tuneNotation);
            	}
            }
            catch (Exception e)
            { e.printStackTrace(); }
           }
        }
        catch (InterruptedException e)
        { e.printStackTrace(); }
      }
    }

    public void changedUpdate(DocumentEvent e)
    {
    }

    public void insertUpdate(DocumentEvent e)
    {
      synchronized(m_mutex)
      {
        m_mutex.notify();
        m_idleTime=0;
      }
    }

    public void removeUpdate(DocumentEvent e)
    {
      synchronized(m_mutex)
      {
        m_mutex.notify();
        m_idleTime=0;
      }
    }
    public void tuneBegin()
    { m_parsingEvents.removeAllElements(); }

    public void tuneEnd(Tune tune)
    {
      if (m_enableColoring)
      {
        //===========================================================THOSE CALLBACK CREATE A DEADLOCK WHEN SETTING TEXT ATTRIBUTES.
        try
        {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              redrawTune();
            }
          });
        }
        catch (Exception e)
        { e.printStackTrace(); }
      }
    }

    public void validToken(final TokenEvent evt)
    {
      m_parsingEvents.addElement(evt);
    }

    private void redrawTune()
    {
      EventObject event = null;
      for (int i=0; i<m_parsingEvents.size(); i++)
      {
        event = (EventObject)m_parsingEvents.elementAt(i);
        if (event instanceof TokenEvent)
        {
          Token token = ((TokenEvent)event).getToken();
          if (token!=null)
          try
          {
            Style att = m_defaultStyle;
            AbcTokenType tokenType = (AbcTokenType)token.getType();
            if (tokenType.isField())
              att = m_fieldStyle;
            else
            if (tokenType.equals(AbcTokenType.TUPLET_SPEC) || tokenType.equals(AbcTokenType.BROKEN_RHYTHM))
              att = m_rhythmStyle;
            else if (tokenType.equals(AbcTokenType.BARLINE) || tokenType.equals(AbcTokenType.REPEAT_OPEN) ||
                     tokenType.equals(AbcTokenType.REPEAT_CLOSE) || tokenType.equals(AbcTokenType.NTH_REPEAT))
              att = m_barStyle;
            else
            if (token.getType().equals(AbcTokenType.COMMENT))
              att = m_commentStyle;
            else
              if (token.getType().equals(AbcTokenType.TEXT))
              {
                if(m_contextForText.equals(AbcTokenType.COMMENT))
                  att = m_commentStyle;
                else
                  att = m_textStyle;
              }
                else
                if (token.getType().equals(AbcTokenType.BASE_NOTE))
                  if (AbcTokenType.GRACING_BEGIN.equals(m_contextForNote))
                    att = m_gracingStyle;
                  else
                    att = m_baseNoteStyle;
                else
                  if (token.getType().equals(AbcTokenType.GRACING_BEGIN))
                  {
                    m_contextForNote = AbcTokenType.GRACING_BEGIN;
                    att = m_gracingStyle;
                  }
                  else
                    if (token.getType().equals(AbcTokenType.GRACING_END))
                    {
                      m_contextForNote = null;
                      att = m_gracingStyle;
                    }
            m_document.setCharacterAttributes(token.getPosition().getCharactersOffset(),
                                              token.getValue().length(), att, true);
            m_contextForText = token.getType();
          }
          catch (Exception e)
          { e.printStackTrace(); }
          else
          {
              //This is an invalid token event.
            //System.out.println("null token in " + event);
          }
        }
        else
          if (event instanceof InvalidCharacterEvent)
          {
            InvalidCharacterEvent evt = (InvalidCharacterEvent)event;
            m_document.setCharacterAttributes(evt.getPosition().getCharactersOffset(), 1, m_errorStyle, true);
          }
          else
          if (event instanceof InvalidTokenEvent)
          {
            InvalidTokenEvent evt = (InvalidTokenEvent)event;
            m_document.setCharacterAttributes(evt.getPosition().getCharactersOffset(), 1, m_errorStyle, true);
          }
      }
    }

    public void invalidToken(InvalidTokenEvent evt)
    { m_parsingEvents.addElement(evt); }

    public void invalidCharacter(InvalidCharacterEvent evt)
    { m_parsingEvents.addElement(evt); }
  }



}
