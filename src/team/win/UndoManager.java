package team.win;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class UndoManager {
	
	public WhiteBoardView mWhiteBoardView;
	
	public UndoManager() {
		super();
	}
	
	public void setContentView(WhiteBoardView view) {
		mWhiteBoardView = view;
	}
	
	public void undo() {
		
		mWhiteBoardView.setNeedsRedraw();

		/*for (Primitive p: mDataStore.getPrimitiveList())
		{
			mWhiteBoardView.drawPath(p);
		}
		Log.i("New", String.format("%d", mDataStore.getPrimitiveList().size()));
		setDataStore(mWhiteBoardView.getDataStore());*/
	}

}
