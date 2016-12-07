package osmanagement.main;

import java.util.ArrayList;

import javax.swing.JFrame;

public class MainStartInterface extends StartInterface {

	private JFrame frame = new JFrame("osManagement");;
	private ArrayList<FunctionItem> funcItems = new ArrayList<FunctionItem>();
	
	public static void main(String[] args) {
		new MainStartInterface().execute();
	}

	public void execute() {
		execute(frame, funcItems);		
	}

	void executeFuncItem(int index) {
		executeFuncItem(index, frame, funcItems);
	}

	void addFuncItems() {
		addFuncItems(funcItems);		
	}
}
