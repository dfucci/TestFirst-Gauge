package gauge;

import java.util.Vector;

public class ActiveFile {

	protected String fileName;
	protected long activeTime;
	protected long idleTime;
	protected int size;
	public Vector<String> fNames = new Vector<String>();
	
	protected boolean included = false;
	protected boolean isTestCode = false;
	public boolean isEditing = false;

}
