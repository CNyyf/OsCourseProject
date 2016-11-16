package osmanagement;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MemAlloc implements FuctionItem {
	private volatile MemList freeList = new MemList();//MemBlock(-1, 0, MEMORY_SIZE);
	private volatile MemList busyList = new MemList();

	private final static int MEMORY_SIZE = 640;
	private final static int BORDER = 20;
	private final static int MEMORY_WIDTH = 60;
	private final static int OUTPUT_WIDTH = 150;
	private final static int OUTPUT_HEIGHT = 300;
	private final static int FIELD_WIDTH = 60;
	private final static int FIELD_HEIGHT = 20;
	private int allocateMethod = 0;
	private volatile int tidForAlloc = 0;
	private boolean flagAutoOp = false;
	private boolean flagExample = false;
	private JFrame frame;
	private JPanel memBlock[] = new JPanel[MEMORY_SIZE];
	private JTextArea textFree = new JTextArea(3, OUTPUT_WIDTH);
	private JTextArea textBusy = new JTextArea(3, OUTPUT_WIDTH);
	private JTextField textAllocSize = new JTextField(FIELD_WIDTH);
	private JTextField textFreeTid = new JTextField(FIELD_WIDTH);
	private JTextField textCurrentMethod = new JTextField(FIELD_WIDTH);
	private JTextField textOutput = new JTextField(OUTPUT_WIDTH*2);
	private JButton buttonAllocate = new JButton("分配");
	private JButton buttonFree = new JButton("释放");
	private JButton buttonAllocateAuto = new JButton("随机分配空间");
	private JButton buttonFreeAuto = new JButton("随机释放作业");
	private JButton buttonBestFit = new JButton("最佳适应分配");
	private JButton buttonFirstFit = new JButton("最先适应分配");
	private JButton buttonAutoOp = new JButton("自动测试");
	private JButton buttonAutoOpStop = new JButton("停止自动测试");
	private JButton buttonExample = new JButton("载入课件样例");
	private JButton buttonClear = new JButton("重置空间");
	private JLabel label[] = new JLabel[7];
	private static Lock mtxOp= new ReentrantLock();
	private static Random randNum = new Random();
	
	private static Color colorFree = Color.cyan;
	private static Color colorBusy = Color.red;
	private static Color colorUnEditable = Color.lightGray;
	
	private boolean allocateMem(int tid, int size, MemBlock mb){
		if(busyList.findByTid(tid) != null){
			textOutput.setText("Already allocated Memory.");
			return false;
		}
		if(size > mb.size){
			textOutput.setText("No enough memory(" + size + "KB) when allocating.");
			return false;
		}
		if(size < 1){
			textOutput.setText("You must allocate more than 0 KB memory.");
			return false;
		}
		if(size < mb.size){
			busyList.add(new MemBlock(tid, mb.start, size));
			mb.start += size;
			mb.size -= size;
		}
		else{
			busyList.add(new MemBlock(tid, mb.start, size));
			freeList.erase(mb);
		}
		textOutput.setText("Success to allocate " + size + " KB memory to thread " + tid + ".");
		paint();
		return true;
	}
	private boolean freeMem(MemBlock mb){
		if(mb == null){
			textOutput.setText("Cannot find the thread.");
			return false;
		}
		MemBlock itmb = freeList.head();
		MemBlock lastmb = null;
		MemBlock nextmb = null;
		int mbStart = mb.start;
		int mbEnd = mb.end();
		//搜索释放的内存在空闲表内的位置
		for(int i = 0; i != freeList.counter(); i++){
			if(itmb.start >= mbEnd){
				nextmb = itmb;
				if(itmb != freeList.head()){
					lastmb = freeList.last(itmb);	
				}
				break;
			}
			itmb = freeList.next(itmb);
		}
		if(nextmb == null){
			lastmb = freeList.tail();
		}
		//先把该空间置入空闲表，然后和前后合并
		busyList.erase(mb);
		textOutput.setText("Success to free " + mb.size + "KB memory from thread " + mb.tid + ".");
		mb = new MemBlock(-1, mb.start, mb.size);
		freeList.insert(lastmb, mb);
		if(lastmb != null){
			if(lastmb.end() == mbStart){
				mb.start -= lastmb.size;
				mb.size += lastmb.size;
				freeList.erase(lastmb);
			}
		}
		if(nextmb != null){
			if(nextmb.start == mbEnd){
				mb.size += nextmb.size;
				freeList.erase(nextmb);
			}
		}
		paint();
		return true;
	}
	private boolean freeMemByTid(int tid){
		MemBlock itmb = busyList.findByTid(tid);
		if(itmb == null){
			textOutput.setText("Cannot find the thread.");
			return false;
		}
		else{
			freeMem(itmb);
		}
		return true;
	}
	private boolean freeMemRandom(){
		MemBlock itmb = busyList.head();
		if(itmb == null){
			textOutput.setText("No thread to free.");
			return false;
		}
		else{
			int location = randNum.nextInt(busyList.counter());
			for(int i = 0; i != location; i++){
				itmb = busyList.next(itmb);
			}
			freeMem(itmb);
		}
		return true;
	}
	private boolean bestFitAllocate(int tid, int size){
		MemBlock freeBlock = freeList.findLargest(size);
		if(freeBlock == null){
			textOutput.setText("No enough memory(" + size + "KB) when allocating.");
			return false;
		}
		else{
			return allocateMem(tid, size, freeBlock);
		}
	}
	private boolean firstFitAllocate(int tid, int size){
		MemBlock freeBlock = freeList.findNext(null, size);
		if(freeBlock == null){
			textOutput.setText("No enough memory(" + size + "KB) when allocating.");
			return false;
		}
		else{
			return allocateMem(tid, size, freeBlock);
		}
	}
	private boolean methodAllocate(int tid, int size){
		tidForAlloc++;
		switch(allocateMethod){
		case 0:
			return bestFitAllocate(tid, size);
		case 1:
			return firstFitAllocate(tid, size);
		default:
			return false;
		}
	}
	private boolean allocateMemRandom(){
		int size = randNum.nextInt(MEMORY_SIZE* 2/3);
		size = size*size*size/MEMORY_SIZE/MEMORY_SIZE + 10;
		System.out.println(size);
		methodAllocate(tidForAlloc, size);
		return true;
	}
	private boolean autoOp(){
		mtxOp.lock();
		int tid = tidForAlloc;
		int size = randNum.nextInt(MEMORY_SIZE);
		boolean success;
		size = size*size*size/MEMORY_SIZE/MEMORY_SIZE * 1/2 + 10;
		success = methodAllocate(tidForAlloc, size);
		paint();
		mtxOp.unlock();
		
		int sleepTime = randNum.nextInt(4000);
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mtxOp.lock();
		if(success){
			freeMemByTid(tid);
			paint();
		}
		mtxOp.unlock();
		return true;
	}
	private boolean autoOpLoop(){
		clear();
		flagAutoOp = true;
		for(;flagAutoOp;){
			Thread thread = new Thread(new Runnable()
				{public void run(){autoOp();}});
			thread.start();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return true;
	}
	private boolean example(){
		clear();
		int exampleList[][] = {{1,130},{2,60},{3,100},{2,0},{4,200},{3,0},{1,0},{5,140},{6,60},{7,50},{6,0}};
		flagExample = true;
		for(int i = 0; i != 11 && flagExample; i++){
			if(exampleList[i][1] == 0){
				freeMemByTid(exampleList[i][0]);
			}
			else{
				methodAllocate(exampleList[i][0], exampleList[i][1]);
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private boolean clear(){
		flagAutoOp = false;
		flagExample = false;
		freeList.clear();
		busyList.clear();
		tidForAlloc = 0;
		freeList.add(new MemBlock(-1, 0, MEMORY_SIZE));
		for(int i = 0; i != 640; i++){
			memBlock[i].setBackground(colorFree);
			//System.out.println(i);
		}
		paint();
		return true;
	}
	private boolean paint(){
		textFree.setText("start\tsize\tend\n");
		textBusy.setText("id\tstart\tsize\tend\n");
		for(MemBlock itfl : freeList.list) {
			textFree.append(itfl.start + "\t" + itfl.size + "\t" + itfl.end() + "\n");
			for(int j = itfl.start; j != itfl.end(); j++){
				memBlock[j].setBackground(colorFree);
			}
		}
		for(MemBlock itbl : busyList.list) {
			textBusy.append(itbl.tid + "\t" + itbl.start + "\t" + itbl.size + "\t" + itbl.end() + "\n");
			for(int j = itbl.start; j != itbl.end(); j++){
				memBlock[j].setBackground(colorBusy);
			}
		}
		return true;
	}
	public void init(){
		frame = new JFrame("osManagement_memAlloc");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(BORDER*4 + MEMORY_WIDTH + OUTPUT_WIDTH*2 + 20, BORDER*2 + MEMORY_SIZE + 40);
		frame.setVisible(true);
		frame.setLayout(null);
		//内存展示
		for(int i = 0; i != 640; i++){
			memBlock[i] = new JPanel();
			frame.add(memBlock[i]);
			memBlock[i].setVisible(true);
			memBlock[i].setBounds(BORDER, BORDER + i, MEMORY_WIDTH, 1);
		}
		//label
		for(int i = 0; i != 7; i++){
			label[i] = new JLabel();
			label[i].setVisible(true);
			frame.add(label[i]);
		}
		label[0].setText("空闲空间");
		label[0].setBounds(BORDER*2 + MEMORY_WIDTH, BORDER, OUTPUT_WIDTH, FIELD_HEIGHT);
		label[1].setText("已用空间");
		label[1].setBounds(BORDER*3 + MEMORY_WIDTH + OUTPUT_WIDTH, BORDER, OUTPUT_WIDTH, FIELD_HEIGHT);
		label[2].setText("最近指令执行状态:");
		label[2].setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT + OUTPUT_HEIGHT, OUTPUT_WIDTH*2, FIELD_HEIGHT);
		label[3].setText("请输入要分配的空间大小:");
		label[3].setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*3 + OUTPUT_HEIGHT, OUTPUT_WIDTH*2, FIELD_HEIGHT);
		label[4].setText("请输入要释放的作业id:");
		label[4].setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*5 + OUTPUT_HEIGHT, OUTPUT_WIDTH*2, FIELD_HEIGHT);
		label[5].setText("选择分配算法（同时重置空间）:");
		label[5].setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*7 + OUTPUT_HEIGHT, OUTPUT_WIDTH*2, FIELD_HEIGHT);
		label[6].setText("自动操作:");
		label[6].setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*12 + OUTPUT_HEIGHT, OUTPUT_WIDTH*2, FIELD_HEIGHT);
		//只读textArea
		frame.add(textFree);
		textFree.setVisible(true);
		textFree.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT, OUTPUT_WIDTH, OUTPUT_HEIGHT);
		textFree.setEditable(false);
		textFree.setTabSize(3);
		textFree.setBackground(colorUnEditable);
		//JScrollPane scroll = new JScrollPane(textFree);
		//frame.add(scroll);
		//scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		//textFree.add(scroll); 
		frame.add(textBusy);
		textBusy.setVisible(true);
		textBusy.setBounds(BORDER*3 + MEMORY_WIDTH + OUTPUT_WIDTH, BORDER + FIELD_HEIGHT, OUTPUT_WIDTH, OUTPUT_HEIGHT);
		textBusy.setEditable(false);
		textBusy.setTabSize(3);
		textBusy.setBackground(colorUnEditable);
		//只读textField
		frame.add(textOutput);
		textOutput.setVisible(true);
		textOutput.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*2 + OUTPUT_HEIGHT, OUTPUT_WIDTH*2 + BORDER, FIELD_HEIGHT);
		textOutput.setEditable(false);
		textOutput.setBackground(colorUnEditable);
		frame.add(textCurrentMethod);
		textCurrentMethod.setVisible(true);
		textCurrentMethod.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*8 + OUTPUT_HEIGHT, OUTPUT_WIDTH*2 + BORDER, FIELD_HEIGHT);
		textCurrentMethod.setEditable(false);
		textCurrentMethod.setBackground(colorUnEditable);
		textCurrentMethod.setText("当前算法：最佳适应分配");
		//写入textField
		frame.add(textAllocSize);
		textAllocSize.setVisible(true);
		textAllocSize.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*4 + OUTPUT_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
		frame.add(textFreeTid);
		textFreeTid.setVisible(true);
		textFreeTid.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*6 + OUTPUT_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
		//button
		frame.add(buttonAllocate);
		buttonAllocate.setVisible(true);
		buttonAllocate.setBounds(BORDER*5/2 + MEMORY_WIDTH + FIELD_WIDTH, BORDER + FIELD_HEIGHT*4 + OUTPUT_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
		frame.add(buttonFree);
		buttonFree.setVisible(true);
		buttonFree.setBounds(BORDER*5/2 + MEMORY_WIDTH + FIELD_WIDTH, BORDER + FIELD_HEIGHT*6 + OUTPUT_HEIGHT, FIELD_WIDTH, FIELD_HEIGHT);
		frame.add(buttonAllocateAuto);
		buttonAllocateAuto.setVisible(true);
		buttonAllocateAuto.setBounds(BORDER*3 + MEMORY_WIDTH + FIELD_WIDTH*2, BORDER + FIELD_HEIGHT*4 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		frame.add(buttonFreeAuto);
		buttonFreeAuto.setVisible(true);
		buttonFreeAuto.setBounds(BORDER*3 + MEMORY_WIDTH + FIELD_WIDTH*2, BORDER + FIELD_HEIGHT*6 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		frame.add(buttonBestFit);
		buttonBestFit.setVisible(true);
		buttonBestFit.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*9 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		frame.add(buttonFirstFit);
		buttonFirstFit.setVisible(true);
		buttonFirstFit.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*10 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		frame.add(buttonAutoOp);
		buttonAutoOp.setVisible(true);
		buttonAutoOp.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*13 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		frame.add(buttonAutoOpStop);
		buttonAutoOpStop.setVisible(true);
		buttonAutoOpStop.setBounds(BORDER*2 + MEMORY_WIDTH, BORDER + FIELD_HEIGHT*14 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		frame.add(buttonExample);
		buttonExample.setVisible(true);
		buttonExample.setBounds(BORDER*3 + MEMORY_WIDTH + FIELD_WIDTH*2, BORDER + FIELD_HEIGHT*13 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		frame.add(buttonClear);
		buttonClear.setVisible(true);
		buttonClear.setBounds(BORDER*3 + MEMORY_WIDTH + FIELD_WIDTH*2, BORDER + FIELD_HEIGHT*14 + OUTPUT_HEIGHT, FIELD_WIDTH*2, FIELD_HEIGHT);
		
		buttonAllocate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				String s = textAllocSize.getText();
				textAllocSize.setText("");
				boolean b = s.matches("[0-9]*") && s.length()>0;
				if(!b){
					textOutput.setText("Please input a natural number(unsigned integer).");
					return;
				}
				int size = Integer.parseInt(s);
				methodAllocate(tidForAlloc, size);
			}});
		buttonFree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				String s = textFreeTid.getText();
				textFreeTid.setText("");
				boolean b = s.matches("[0-9]*") && s.length()>0;
				if(!b){
					textOutput.setText("Please input a natural number(unsigned integer).");
					return;
				}
				int tid = Integer.parseInt(s);
				freeMemByTid(tid);
			}});
		buttonAllocateAuto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				allocateMemRandom();
			}});
		buttonFreeAuto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				freeMemRandom();
			}});
		buttonBestFit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				allocateMethod = 0;
				textCurrentMethod.setText("当前算法：最佳适应分配");
				clear();
			}});
		buttonFirstFit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				allocateMethod = 1;
				textCurrentMethod.setText("当前算法：最先适应分配");
				clear();
			}});
		buttonAutoOp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				if(!flagAutoOp){
					Thread thread = new Thread(new Runnable()
						{public void run(){autoOpLoop();}});
					thread.start();
				}
			}});
		buttonAutoOpStop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				flagAutoOp = false;
			}});
		buttonExample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				Thread thread = new Thread(new Runnable()
					{public void run(){example();}});
				thread.start();
			}});
		buttonClear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{				
				clear();
			}});
		clear();
	}
	@Override
	public void excuteFuction() {
		MemAlloc ma = new MemAlloc();
		ma.init();
	}
	
}

class MemList{
	ArrayList<MemBlock> list = new ArrayList<MemBlock>();
	
	MemBlock erase(MemBlock mb){
		if(list.remove(mb)) {
			return mb;
		}
		return null;
	}
	MemBlock insert(MemBlock location, MemBlock mb){
		if(mb == null) return null;
		if(location == null) {
			list.add(0, mb);
		}
		else {
			int loc = list.indexOf(location);
			if(loc < 0) {
				return null;
			}
			list.add(loc + 1, mb);
		}
		return mb;
	}
	MemBlock add(MemBlock mb){
		if(mb == null) return null;
		list.add(mb);
		return mb;
	}
	MemBlock findLargest(int size){
		MemBlock product = null;
		int currentSize = 0;
		for(MemBlock itmb: list) {
			if(itmb.size >= size && itmb.size > currentSize) {
				product = itmb;
				currentSize = product.size;
			}
		}
		return product;
	}
	MemBlock findNext(MemBlock start, int size){
		if(start == null){
			start = head();
		}
		MemBlock itmb = start;
		for(int i = 0; i != counter(); i++){
			if(itmb.size >= size){
				return itmb;
			}
			itmb = next(itmb);
		}
		return null;
	}
	MemBlock findByTid(int tid){
		for(MemBlock itmb:list) {
			if(itmb.tid == tid) {
				return itmb;
			}
		}
		return null;
	}
	boolean clear(){
		list.clear();
		return true;
	}
	MemBlock head() {
		if(list.size() == 0) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	MemBlock tail() {
		if(list.size() == 0) {
			return null;
		}
		else {
			return list.get(list.size() - 1);
		}
	}
	int counter() {
		return list.size();
	}
	MemBlock last(MemBlock mb) {
		int loc = list.indexOf(mb);
		if(loc < 0) {
			return null;
		}
		loc--;
		if(loc < 0) {
			loc += list.size();
		}
		return list.get(loc);
	}
	MemBlock next(MemBlock mb) {
		int loc = list.indexOf(mb);
		if(loc < 0) {
			return null;
		}
		loc++;
		if(loc >= list.size()) {
			loc -= list.size();
		}
		return list.get(loc);
	}
}

class MemBlock{
	int tid = -1;
	int start = 0;
	int size = 0;
	MemBlock(int t, int s, int l){
		tid = t;
		start = s;
		size = l;
	}
	int end(){
		return start + size;
	}
}