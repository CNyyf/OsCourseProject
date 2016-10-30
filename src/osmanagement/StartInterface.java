package osmanagement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class StartInterface {
	private JFrame frame;
	private JLabel label = new JLabel();
	private JButton buttonCPU = new JButton("十字路口模拟");
	private JButton buttonMemAlloc = new JButton("动态分区分配");
	private JButton buttonPaging = new JButton("页式存储管理");
	private JButton buttonDoc = new JButton("文件系统模拟");
	
	StartInterface(){
		frame = new JFrame("osManagement");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 200);
		frame.setVisible(true);
		frame.setLayout(null);
		
		frame.add(label);
		label.setBounds(20, 20, 260, 30);
		label.setText("请选择您要打开的模拟器：");

		frame.add(buttonCPU);
		buttonCPU.setBounds(20, 90, 120, 40);
		frame.add(buttonMemAlloc);
		buttonMemAlloc.setBounds(160, 90, 120, 40);
		frame.add(buttonPaging);
		buttonPaging.setBounds(300, 90, 120, 40);
		frame.add(buttonDoc);
		buttonDoc.setBounds(440, 90, 120, 40);
		
		buttonCPU.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				Thread th = new Thread(new Runnable()
				{public void run(){TrafficLine.startRunning();}});
				th.start();
				frame.setVisible(false);
			}});
		buttonMemAlloc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new MemAlloc();
				frame.setVisible(false);
			}});
		buttonPaging.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new Paging();
				frame.setVisible(false);
			}});
		buttonDoc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				new DocSimulator();
				frame.setVisible(false);
			}});
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new StartInterface();
	}

}
