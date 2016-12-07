package osmanagement.main;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import osmanagement.dmemalloc.MemAlloc;
import osmanagement.file.DocSimulator;
import osmanagement.pagging.Paging;
import osmanagement.traffic.TrafficLine;

public abstract class StartInterface {
	
	void execute(JFrame frame, ArrayList<FunctionItem> funcItems){
		final int BTN_CNT = 4;
		final int BTN_HIT = 40;
		final int BTN_WID = 120;
		final int BTN_Y = 90;
		final int BTN_X = 20;
		JLabel label = new JLabel();
		JButton[] buttons = new JButton[BTN_CNT];
		
		addFuncItems(funcItems);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(BTN_X * (BTN_CNT + 2) + BTN_WID * BTN_CNT, 200);
		frame.setVisible(true);
		frame.setLayout(null);
		
		frame.add(label);
		label.setBounds(20, 20, 260, 30);
		label.setText("请选择您要打开的模拟器：");
		
		for(int i = 0; i != BTN_CNT; i++) {
			buttons[i] = new JButton();
			frame.add(buttons[i]);
			buttons[i].setBounds(BTN_X*(i + 1) + BTN_WID*i, BTN_Y, BTN_WID, BTN_HIT);
		}
		buttons[0].setText("十字路口模拟");
		buttons[1].setText("动态分区分配");
		buttons[2].setText("页式存储管理");
		buttons[3].setText("文件系统模拟");
		
		buttons[0].addActionListener((ActionEvent e)->{executeFuncItem(0);});
		buttons[1].addActionListener((ActionEvent e)->{executeFuncItem(1);});
		buttons[2].addActionListener((ActionEvent e)->{executeFuncItem(2);});
		buttons[3].addActionListener((ActionEvent e)->{executeFuncItem(3);});
	}
	void executeFuncItem(int index, JFrame frame, ArrayList<FunctionItem> funcItems) {
		if(index > -1 && index < funcItems.size()) {
			funcItems.get(index).excuteFuction();
			frame.setVisible(false);
		}
	}
	void addFuncItems(ArrayList<FunctionItem> funcItems) {
		funcItems.add(new TrafficLine());
		funcItems.add(new MemAlloc());
		funcItems.add(new Paging());
		funcItems.add(new DocSimulator());
	}
	void execute(){}
	void executeFuncItem(int index){}
	void addFuncItems(){}
}
