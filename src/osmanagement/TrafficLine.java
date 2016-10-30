package osmanagement;

import java.awt.Color;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class TrafficLine {
	static final int HALF_LINE_NUMBER = 2;//车道数，在开始时可以更改
	static final int LINE_NUMBER = HALF_LINE_NUMBER * 2;
	static final int IN_LENGTH = 6;
	static final int ON_LENGTH = LINE_NUMBER;
	static final int OUT_LENGTH = IN_LENGTH;
	static final int ENTRANCE = IN_LENGTH - 1;
	static final int OFF_LENGTH =  Math.max(IN_LENGTH, OUT_LENGTH);
	static final int IN_ON_LENGTH = IN_LENGTH + ON_LENGTH;
	static final int WHOLE_LENGTH = IN_LENGTH + ON_LENGTH + OUT_LENGTH;
	static final int LARGE_WHOLE_LENGTH = OFF_LENGTH * 2 + ON_LENGTH;
	static int lightTime = 8000;
	static int moveTime = 200;
	static int randomSetTime = 100;
	static int printTime = 100;
	
	static boolean ableCross = true;
	static boolean runSign = true;
	static boolean randomSetSign = true;
	volatile static int ableSignal = 0;
	volatile private Car[][]carQueue = new Car[LINE_NUMBER][WHOLE_LENGTH];
	private int ableValue;
	volatile private int crossCarNumber = 0;
	private Random randNum = new Random();

	static int borderLength = 20;
	static int blockLength = 20;
	static int carLength = 16;
	static int carWidth = 10;
	static JFrame frame = new JFrame("osManagement_traffic");
	static Color colorRoad = Color.gray;
	static Color colorMidelLine = Color.yellow;
	static Color colorCarNormalAhead = Color.cyan;
	static Color colorCarNormalTurn = Color.blue;
	static Color colorCarSpecialAhead = Color.white;
	static Color colorCarSpecialTurn = Color.lightGray;
	static Panel carMap[][][] = new Panel[2][LINE_NUMBER][LARGE_WHOLE_LENGTH];
	static Panel signalLight[] = new Panel[4];
	static Panel roads[] = new Panel[11];
	static JLabel labels[] = new JLabel[4];
	static JButton button[] = new JButton[10];
	public TrafficLine(int abv)
	{
		ableValue = abv;
		crossCarNumber = 0;
		for (int j = 0; j != LINE_NUMBER; j++)
		{
			for (int i = 0; i != WHOLE_LENGTH; i++)
			{
				carQueue[j][i] = null;
			}
		}
	}
	
	private int popCarOnCross(TrafficLine turnTl)
	{
		//System.out.println(ableValue + "!!!" + "crossCarNumber");
		int abs = ableSignal;
		//System.out.println("PopCar");

		//已进入路口未离开路口的车辆向前移动
		for (int j = 0; j != LINE_NUMBER; j++)
		{
			if (carQueue[j][IN_ON_LENGTH - 1] != null && carQueue[j][IN_ON_LENGTH] == null)
			{
				carQueue[j][IN_ON_LENGTH] = carQueue[j][IN_ON_LENGTH - 1];
				carQueue[j][IN_ON_LENGTH - 1] = null;
				crossCarNumber--;
				if (crossCarNumber == 0)
				{
					ableCross = true;
					//System.out.println(ableValue + " endtagA");
				}
			}
			for (int i = IN_ON_LENGTH - 1; i > IN_LENGTH + 1; i--)
			{
				if(carQueue[j][i - 1] != null && carQueue[j][i] == null)
				{
					carQueue[j][i] = carQueue[j][i - 1];
					carQueue[j][i - 1] = null;
				}
			}
			//carQueue[j][IN_LENGTH + 1] = null;
			if(carQueue[j][IN_LENGTH] != null)
			{
				if(carQueue[j][IN_LENGTH].direct == 0)
				{
					if(carQueue[j][IN_LENGTH + 1] == null)
					{

						carQueue[j][IN_LENGTH + 1] = carQueue[j][IN_LENGTH];
						carQueue[j][IN_LENGTH] = null;
					}
				}
				else
				{
					int direct;
					if(j == ableValue)
						direct = 1;
					else direct = 0;
					if(turnTl.carQueue[direct][IN_LENGTH + 1] == null)
					{
						turnTl.carQueue[direct][IN_LENGTH + ON_LENGTH] = carQueue[j][IN_LENGTH];
						carQueue[j][IN_LENGTH] = null;
						crossCarNumber--;
						if (crossCarNumber == 0)
						{
							ableCross = true;
							//System.out.println(ableValue + " endtagA");
						}
					}
				}
			}
		}
		
		//红灯时只有特种车可进入入口，后车向前跟进
		for (int j = 0; j != LINE_NUMBER; j++)
		{
			if (carQueue[j][ENTRANCE] != null && carQueue[j][IN_LENGTH] == null)
			{
				crossCarNumber++;
				if (crossCarNumber == 1)
				{
					if(ableCross)
					{
						ableCross = false;
					}
					else
					{
						crossCarNumber--;
						return 0;
					}
					//System.out.println(ableValue + " start");
				}
				if (abs == ableValue || (carQueue[j][ENTRANCE] == null) ? true : carQueue[j][ENTRANCE].priority < 2)
				{
					carQueue[j][IN_LENGTH] = carQueue[j][IN_LENGTH - 1];
					carQueue[j][IN_LENGTH - 1] = null;
				}
				else if (crossCarNumber == 1)
				{
					crossCarNumber--;
					ableCross = true;
					//System.out.println(ableValue + " endtagB");
				}
				else
				{
					crossCarNumber--;
				}
			}
		}
		return 1;
	}
	
	private int popCarOnLine()
	{
		//未到达路口或已离开路口的车辆向前移动
		for (int j = 0; j != LINE_NUMBER; j++)
		{
			for (int i = WHOLE_LENGTH - 1; i > IN_ON_LENGTH; i--)
			{
				carQueue[j][i] = carQueue[j][i - 1];
			}
			carQueue[j][IN_ON_LENGTH] = null;
		}
		try {
			Thread.sleep(moveTime/4);
		} catch (InterruptedException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		for (int j = LINE_NUMBER - 1; j != -1; j--)//为了先运算靠内车道，反向循环
		{
			for (int i = IN_LENGTH - 1; i > 0; i--)
			{
				if (carQueue[j][i - 1] != null && carQueue[j][i] == null)
				{
					carQueue[j][i] = carQueue[j][i - 1];
					carQueue[j][i - 1] = null;
				}
				else if(carQueue[j][i - 1] != null)
				{
					if(j < LINE_NUMBER - 2)
					{
						if(carQueue[j][i - 1].priority == 0 && carQueue[j][i - 1].direct == 0 && carQueue[j + 2][i] == null)
						{
							carQueue[j + 2][i] = carQueue[j][i - 1];
							carQueue[j][i - 1] = null;
							continue;
						}
					}
					if(j > 1)
					{
						if(carQueue[j][i - 1].priority == 0 && carQueue[j][i - 1].direct == 0 && carQueue[j - 2][i] == null)
						{
							carQueue[j - 2][i] = carQueue[j][i - 1];
							carQueue[j][i - 1] = null;
						}
					}
				}
			}
		}
		return 1;
	}
	
	boolean setCar(int lid, int nb, int pr, int dr)
	{
		lid %= LINE_NUMBER;
		//System.out.println(lid);
		if(dr == 1)
			lid %= 2;
		if (carQueue[lid][0] == null)
		{
			carQueue[lid][0] = new Car(nb, pr, dr);
			return true;
		}
		return false;
	}

	private boolean randomSetCar(int nb)
	{
		int randNumber = 0;
		int lid = 0;
		int direct = 0;
		randNumber = randNum.nextInt(360360);
		lid = randNumber % LINE_NUMBER;
		randNumber %= 97;
		direct = randNum.nextInt(5);
		if(direct != 1)
			direct = 0;
		if (randNumber < 25)
		{
			if (randNumber < 20)
				setCar(lid, nb, 2, direct);
			else
				setCar(lid, nb, 0, direct);
			return true;
		}
		return false;
	}
	
	static private int turnSignal()
	{
		if (ableSignal == 0)
		{
			ableSignal = 1;
		}
		else if (ableSignal == 1)
		{
			ableSignal = 0;
		}
		return 1;
	}
	
	static private int drawInit(TrafficLine tl1, TrafficLine tl2)
	{
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize((int) (borderLength * 2 + blockLength * (LARGE_WHOLE_LENGTH + 11.5) + 20), borderLength * 2 + blockLength * LARGE_WHOLE_LENGTH + 40);
		frame.setVisible(true);
		frame.setLayout(null);
		
		button[0] = new JButton("停止自动添加车辆");
		button[1] = new JButton("开始自动添加车辆");
		for(int i = 0; i != 2; i++)
		{
			button[i].setBounds((int) (borderLength * 2.5 + blockLength * LARGE_WHOLE_LENGTH) , borderLength + blockLength * (i + 6), blockLength * 7, blockLength);
		}
		for(int i = 2; i != 6; i++)
		{
			button[i] = new JButton("横向");
			button[i].setBounds((int) (borderLength * 2.5 + blockLength * (LARGE_WHOLE_LENGTH + 4)) , borderLength + blockLength * (i - 2), blockLength * 3, blockLength);
		}
		for(int i = 6; i != 10; i++)
		{
			button[i] = new JButton("纵向");
			button[i].setBounds((int) (borderLength * 2.5 + blockLength * (LARGE_WHOLE_LENGTH + 7)) , borderLength + blockLength * (i - 6), blockLength * 3, blockLength);
		}
		for(int i = 0; i != 10; i++)
		{
			button[i].setVisible(true);
			frame.add(button[i]);
		}
		button[0].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(randomSetSign == true)
				{
					randomSetSign = false;
				}
			}});
		button[1].addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(randomSetSign == false)
				{
					randomSetSign = true;
				}
			}
	});
		button[2].addActionListener(new SetCarButton(2, 0, tl1));
		button[3].addActionListener(new SetCarButton(2, 1, tl1));
		button[4].addActionListener(new SetCarButton(0, 0, tl1));
		button[5].addActionListener(new SetCarButton(0, 1, tl1));
		button[6].addActionListener(new SetCarButton(2, 0, tl2));
		button[7].addActionListener(new SetCarButton(2, 1, tl2));
		button[8].addActionListener(new SetCarButton(0, 0, tl2));
		button[9].addActionListener(new SetCarButton(0, 1, tl2));
		//显示车辆，最上层
		for(int j = 0; j != LINE_NUMBER; j++)
			for(int i = 0; i != LARGE_WHOLE_LENGTH; i++)
			{
				carMap[0][j][i] = new Panel();
				carMap[1][j][i] = new Panel();
				int	halfLineNumber = j / 2;
				if (j % 2 == 0)
				{
					carMap[0][j][i].setBounds(borderLength + (i) * blockLength + (blockLength - carLength)/2, 
							borderLength + (OFF_LENGTH + ON_LENGTH - 1 - halfLineNumber) * blockLength + (blockLength - carWidth)/2, 
							carLength, carWidth);
					carMap[1][j][i].setBounds(borderLength + (OFF_LENGTH + ON_LENGTH - 1 - halfLineNumber) * blockLength + (blockLength - carWidth)/2, 
							borderLength + (LARGE_WHOLE_LENGTH - 1 - i) * blockLength + (blockLength - carLength)/2, 
							carWidth, carLength);
				}
				else
				{
					carMap[0][j][i].setBounds(borderLength + (LARGE_WHOLE_LENGTH - 1 - i) * blockLength + (blockLength - carLength)/2, 
							borderLength + (OFF_LENGTH + halfLineNumber) * blockLength + (blockLength - carWidth)/2, 
							carLength, carWidth);
					carMap[1][j][i].setBounds(borderLength + (OFF_LENGTH + halfLineNumber) * blockLength + (blockLength - carWidth)/2, 
							borderLength + (i) * blockLength + (blockLength - carLength)/2, 
							carWidth, carLength);
				}
				carMap[0][j][i].setVisible(false);
				carMap[1][j][i].setVisible(false);
				frame.add(carMap[0][j][i]);
				frame.add(carMap[1][j][i]);
			}
		//显示信号灯
		for(int i = 0; i != 4; i++)
		{
			signalLight[i] = new Panel();
			signalLight[i].setVisible(true);
			frame.add(signalLight[i]);
		}
		signalLight[0].setBounds(borderLength + (OFF_LENGTH - 2) * blockLength, borderLength + (OFF_LENGTH + ON_LENGTH) * blockLength, blockLength, blockLength);
		signalLight[1].setBounds(borderLength + (OFF_LENGTH + ON_LENGTH) * blockLength, borderLength + (OFF_LENGTH + ON_LENGTH + 1) * blockLength, blockLength, blockLength);
		signalLight[2].setBounds(borderLength + (OFF_LENGTH + ON_LENGTH + 1) * blockLength, borderLength + (OFF_LENGTH - 1) * blockLength, blockLength, blockLength);
		signalLight[3].setBounds(borderLength + (OFF_LENGTH - 1) * blockLength, borderLength + (OFF_LENGTH - 2) * blockLength, blockLength, blockLength);
		//显示道路信息，最底层
		for(int i = 10 ; i != -1; i--)
		{
			roads[i] = new Panel();
			if(i < 3)
				roads[i].setBackground(colorRoad);
			else if(i < 7)
				roads[i].setBackground(colorMidelLine);
			roads[i].setVisible(true);
			frame.add(roads[i]);
		}
		roads[0].setBounds(borderLength, borderLength + OFF_LENGTH * blockLength, LARGE_WHOLE_LENGTH * blockLength, ON_LENGTH * blockLength);
		roads[1].setBounds(borderLength + OFF_LENGTH * blockLength, borderLength, ON_LENGTH * blockLength, LARGE_WHOLE_LENGTH * blockLength);
		roads[2].setBounds((int) (borderLength * 1.5 + blockLength * LARGE_WHOLE_LENGTH), borderLength, blockLength, blockLength * 4);
		roads[3].setBounds(borderLength, borderLength + LARGE_WHOLE_LENGTH/2 * blockLength - 1, OFF_LENGTH * blockLength, 2);
		roads[4].setBounds(borderLength + LARGE_WHOLE_LENGTH/2 * blockLength - 1, borderLength, 2, OFF_LENGTH * blockLength);
		roads[5].setBounds(borderLength + (OFF_LENGTH + ON_LENGTH) * blockLength, borderLength + LARGE_WHOLE_LENGTH/2 * blockLength - 1, OFF_LENGTH * blockLength, 2);
		roads[6].setBounds(borderLength + LARGE_WHOLE_LENGTH/2 * blockLength - 1, borderLength + (OFF_LENGTH + ON_LENGTH) * blockLength, 2, OFF_LENGTH * blockLength);
		roads[7].setBackground(colorCarNormalAhead);
		roads[8].setBackground(colorCarNormalTurn);
		roads[9].setBackground(colorCarSpecialAhead);
		roads[10].setBackground(colorCarSpecialTurn);
		for(int i = 7; i != 11; i++)
			roads[i].setBounds(borderLength * 2 + blockLength * LARGE_WHOLE_LENGTH - carLength/2, (int) (borderLength + blockLength * (i - 6.5) - carWidth/2), carLength, carWidth);
		//Label信息
		for(int i = 0; i != 4; i++)
		{
			labels[i] = new JLabel();
			labels[i].setVisible(true);
			labels[i].setBounds((int) (borderLength * 2.5 + blockLength * LARGE_WHOLE_LENGTH) , borderLength + blockLength * i, blockLength * 4, blockLength);
			frame.add(labels[i]);
		}
		labels[0].setText("普通直行车");
		labels[1].setText("普通转向车");
		labels[2].setText("特权直行车");
		labels[3].setText("特权转向车");
		try {
			Thread.sleep(printTime/2);
		} catch (InterruptedException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return 1; 
	}
	
	static private int drawFrame(TrafficLine tl1, TrafficLine tl2)
	{
		//循环操作开始，显示车辆状况
		for (int j = 0; j != LINE_NUMBER; j++)
		{
			for (int i = 0; i != WHOLE_LENGTH; i++)
			{
				if(tl1.carQueue[j][i]==null)
					carMap[0][j][i].setVisible(false);
				else
				{
					if(tl1.carQueue[j][i].direct == 1)
					{
						if(tl1.carQueue[j][i].priority == 2)
							carMap[0][j][i].setBackground(colorCarNormalTurn);
						else
							carMap[0][j][i].setBackground(colorCarSpecialTurn);
					}
					else
					{
						if(tl1.carQueue[j][i].priority == 2)
							carMap[0][j][i].setBackground(colorCarNormalAhead);
						else
							carMap[0][j][i].setBackground(colorCarSpecialAhead);
					}
					carMap[0][j][i].setVisible(true);
				}
				if(tl2.carQueue[j][i]==null)
					carMap[1][j][i].setVisible(false);
				else
				{
					if(tl2.carQueue[j][i].direct == 1)
					{
						if(tl2.carQueue[j][i].priority == 2)
							carMap[1][j][i].setBackground(colorCarNormalTurn);
						else
							carMap[1][j][i].setBackground(colorCarSpecialTurn);
						carMap[1][j][i].setVisible(true);
					}
					else
					{
						if(tl2.carQueue[j][i].priority == 2)
							carMap[1][j][i].setBackground(colorCarNormalAhead);
						else
							carMap[1][j][i].setBackground(colorCarSpecialAhead);
						carMap[1][j][i].setVisible(true);
					}
				}
			}
		}
		
		for(int i = 0; i != 4; i++)
		{
			if(i%2 == ableSignal)
				signalLight[i].setBackground(Color.green);
			else
				signalLight[i].setBackground(Color.red);
		}
		return 1;
	}
	
	static public void startRunning()
	{
		TrafficLine tl1 = new TrafficLine(0);
		TrafficLine tl2 = new TrafficLine(1);
		int carNum = 0;
		drawInit(tl1, tl2);
		for(int countFlag = 0; runSign; countFlag++){
			if(countFlag > 79)
			{
				countFlag = 0;
			}
			if(randomSetSign)
			{
				if(tl1.randomSetCar(carNum))
				{
					carNum++;
				}
				if(tl2.randomSetCar(carNum))
				{
					carNum++;
				}
			}
			if(countFlag%2 == 0)
			{
				tl1.popCarOnLine();
				tl1.popCarOnCross(tl2);
				tl2.popCarOnLine();
				tl2.popCarOnCross(tl1);
			}
			if(countFlag%80 == 0)
			{
				turnSignal();
			}
			drawFrame(tl1, tl2);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	static public void main(String[] args) {
		startRunning();
	}
}

class Car{
	int number;
	int priority;
	int direct;
	public Car(int nb, int pr, int dr) 
	{
		number = nb;
		priority = pr;
		direct = dr;
	}
	public Car(int nb, int pr) 
	{
		number = nb;
		priority = pr;
		direct = 0;
	}
}

class SetCarButton implements ActionListener{
	private int priority;
	private int direct;
	private TrafficLine trafficLine;
	static private Random randNum = new Random();
	public SetCarButton(int pr, int dr, TrafficLine tl)
	{
		priority = pr;
		direct = dr;
		trafficLine = tl;
	}
	public void actionPerformed(ActionEvent e)
	{
		int randnum = randNum.nextInt(TrafficLine.LINE_NUMBER);
		trafficLine.setCar(randnum, -1, priority, direct);
	}
}
