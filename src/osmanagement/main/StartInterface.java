package osmanagement.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import osmanagement.dmemalloc.MemAlloc;
import osmanagement.file.DocSimulator;
import osmanagement.pagging.Paging;
import osmanagement.traffic.TrafficLine;

public class StartInterface {
	private static final int BTN_CNT = 4;
	private static final int BTN_HIT = 40;
	private static final int BTN_WID = 120;
	private static final int BTN_Y = 90;
	private static final int BTN_X = 20;
	private JFrame frame;
	private JLabel label = new JLabel();
	private JButton[] buttons = new JButton[BTN_CNT];
	private ArrayList<FunctionItem> funcItems = new ArrayList<FunctionItem>();
	
	StartInterface(){
		addFuncItems();
		
		frame = new JFrame("osManagement");
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
		
		buttons[0].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				executeFuncItem(0);
			}});
		buttons[1].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				executeFuncItem(1);
			}});
		buttons[2].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				executeFuncItem(2);
			}});
		buttons[3].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				executeFuncItem(3);
			}});
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new StartInterface();
	}
	private void executeFuncItem(int index) {
		if(index > -1 && index < funcItems.size()) {
			funcItems.get(index).excuteFuction();
			frame.setVisible(false);
		}
	}
	private void addFuncItems() {
		funcItems.add(new TrafficLine());
		funcItems.add(new MemAlloc());
		funcItems.add(new Paging());
		funcItems.add(new DocSimulator());
	}
}
