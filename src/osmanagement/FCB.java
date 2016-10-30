package osmanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.tree.DefaultMutableTreeNode;

import osmanagement.FCB;

public class FCB {
	static final int BLOCK_SIZE = 10;
	static final int VOLUMN = 64;
	static int countBlockFree = VOLUMN;
	static ArrayList<Integer> listBlockFree = new ArrayList<Integer>();
    static public final int FILE = 0;
    static public final int FOLDER = 1;
    static private String blocksFolderPath = "blocks";
	static private String FCBPath = "blocks/FCB";
    String name;
    private int type;
    ArrayList<Integer> blocks = new ArrayList<Integer>();
    boolean isOpened = false;
    public FCB(String n, int t) {
        name = n;
        type = t;
    }
    public int getType(){
    	return type;
    }
    public String toString() {
        return name;
    }
    static String getFCBPath(){
    	return FCBPath;
    }
    static int fetchFirstBlockFree(){
    	if(listBlockFree.size() == 0){
    		return -1;
    	}
    	int firstBlockFree = listBlockFree.get(0);
    	listBlockFree.remove(0);
    	countBlockFree--;
    	return firstBlockFree;
    }
    static boolean releaseLastBlockFree(int lastBlockFree){
    	listBlockFree.add(new Integer(lastBlockFree));
    	countBlockFree++;
    	return false;
    }
    static String getPath(DefaultMutableTreeNode node){
    	if(node == null){
    		return "";
    	}
    	else{
    		return getPath((DefaultMutableTreeNode) node.getParent()) +"/"+ node;
    	}
    }
    static String getParentPath(DefaultMutableTreeNode node){
    	return getPath((DefaultMutableTreeNode) node.getParent());
    }
    static DefaultMutableTreeNode setNode(DefaultMutableTreeNode root, String p, String n, int t){
    	DefaultMutableTreeNode parent = getNodeByPath(root, p);
    	if(parent == null){
    		return null;
    	}
    	if(((FCB)parent.getUserObject()).type == FCB.FILE){
    		return null;
    	}
    	String name = getName(parent, n);
    	DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FCB(name, t));
    	parent.add(node);
    	return node;
    }
    static DefaultMutableTreeNode getNodeByPath(DefaultMutableTreeNode root, String p){
    	DefaultMutableTreeNode parent = root;
    	String []temp = p.split("/");
    	for(int i = 2; i < temp.length; i++){
    		//System.out.println(temp[i]);
    		if((parent = FCB.getChildByString(parent, temp[i])) == null){
    			break;
    		}
    	}
    	return parent;
    }
    static DefaultMutableTreeNode getChildByString(DefaultMutableTreeNode parent, String n){
    	int num = parent.getChildCount();
    	DefaultMutableTreeNode node;
    	for(int i = 0; i != num; i++){
    		node = (DefaultMutableTreeNode) parent.getChildAt(i);
    		//System.out.println(node + n);
    		if(node.toString().equals(n)){
    			//System.out.println("yes");
    			return node;
    		}
    	}
		//System.out.println("no");
		return null;
    }
    static boolean storeFCB(DefaultMutableTreeNode root, String filePath) {
    	BufferedWriter bufferedwriter = null;
		//String absolutePath = FCB.class.getResource("/").getFile();
		//absolutePath += "/" + filePath;
		//System.out.println(absolutePath);
    	try {
			bufferedwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath))));
			DefaultMutableTreeNode node = root;
			//System.out.println("Success open.");
			while(node != null){
				Object object = node.getUserObject();
				FCB fcb = (FCB)object;
				if(fcb.name == null || fcb.name.equals("")){
					fcb.name = getName((DefaultMutableTreeNode) node.getParent(), "Untitled");
				}
				try {
					bufferedwriter.write(fcb.toString() + " " + FCB.getParentPath(node) + " " + fcb.getType() + " " + fcb.blocks.size());
					for(int i = 0; i != fcb.blocks.size(); i++){
						bufferedwriter.write(" " + fcb.blocks.get(i));
					}
					bufferedwriter.newLine();
					//System.out.println("Success write.");
				} catch (IOException e) {
					System.out.println("File IO Error!");
				}
				node = node.getNextNode();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
		} finally{
			try {
				bufferedwriter.close();
			} catch (IOException e) {
				System.out.println("File Cannot Close!");
			}
		}
    	return true;
    }
    static DefaultMutableTreeNode loadFCB(String folderPath, String FCBfilePath){
    	blocksFolderPath = folderPath;
    	loadFileBlocks();
    	Scanner input = null;
    	DefaultMutableTreeNode root = null;
    	countBlockFree = VOLUMN;
    	try {
			input = new Scanner(new File(FCBfilePath));
			String temp;
			Scanner tempScan = null;
			if(input.hasNextLine()){
				temp = input.nextLine();
				//System.out.println(temp);
				tempScan = new Scanner(temp);
				root = new DefaultMutableTreeNode(new FCB(tempScan.next(), FOLDER));
			}
			else{
				root = new DefaultMutableTreeNode(new FCB("root", FOLDER));
			}
			for(int i = 0; i != VOLUMN; i++){
				listBlockFree.add(new Integer(i));
			}
			while(input.hasNextLine()){
				temp = input.nextLine();
				//System.out.println(temp);
				tempScan = new Scanner(temp);
				String name = tempScan.next();
				String path = tempScan.next();
				int type = tempScan.nextInt();
				int size = tempScan.nextInt();
				DefaultMutableTreeNode node = setNode(root, path, name, type);
				//System.out.println(node.toString() + node.getParent());
				if(node != null && type == FILE && size > 0){
					FCB fcb = (FCB)node.getUserObject();
					for(int i = 0; i != size; i++){
						int address = tempScan.nextInt();
						//System.out.println(address);
						if(address > -1 && address < VOLUMN){
							listBlockFree.remove(new Integer(address));
							fcb.blocks.add(new Integer(address));
							countBlockFree--;
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
			root = new DefaultMutableTreeNode(new FCB("root", FOLDER));
	    	listBlockFree.clear();
	    	for(int i = 0; i != VOLUMN; i++){
	    		listBlockFree.add(new Integer(i));
	    	}
		}
		//System.out.println(+root.getChildCount());
    	return root;
    }
    static boolean loadFileBlocks(){
    	String folderPath = blocksFolderPath;
    	File file =new File(folderPath); 
	    if(!file.exists()){
	    	//System.out.print("不存在");
	    	file.mkdirs();
	    }
	    else if(file.isFile()){
	    	//System.out.print("存在文件");
	    	file.delete();
	    	file.mkdirs();
	    }
	    for(int i = 0; i != VOLUMN; i++){
	    	file = new File(folderPath + "/" + i);
	    	if(file.exists() && file.isDirectory()){
	    		//System.out.println(i + "类型错误");
	    		file.delete();
	    	}
	    	if(!file.exists()){
	    		//System.out.println(i + "新建");
	    		try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    }
    	return true;
    }
    //FCB操作实现
    static String getName(DefaultMutableTreeNode parent, String n){
    	if(parent == null){
    		return n;
    	}
    	String name = n;
    	for(int i = 1; FCB.getChildByString(parent, name) != null; i++){
    		name = n + "_" + i;
    	}
    	return name;
    }
    static DefaultMutableTreeNode format(DefaultMutableTreeNode root){
    	if(root == null){
    		return null;
    	}
    	root.removeAllChildren();
    	countBlockFree = VOLUMN;
    	listBlockFree.clear();
    	for(int i = 0; i != VOLUMN; i++){
    		listBlockFree.add(new Integer(i));
    	}
    	((FCB)root.getUserObject()).name = "root";
    	return root;
    }
    static DefaultMutableTreeNode createFCB(DefaultMutableTreeNode parent, String n, int t){
    	if(parent == null){
    		return null;
    	}
    	FCB parentFcb = (FCB)parent.getUserObject();
    	if(parentFcb.type == FILE){
    		return null;
    	}
    	String name = getName(parent, n);
    	DefaultMutableTreeNode node = new DefaultMutableTreeNode(new FCB(name, t));
    	parent.add(node);
    	return node;
    }
    static boolean deleteFCB(DefaultMutableTreeNode parent){
    	if(parent == null){
    		return false;
    	}
    	FCB fcb = (FCB)parent.getUserObject();
    	if(fcb.isOpened){
    		return false;
    	}
    	if(fcb.type == FOLDER){
    		while(parent.getChildCount() != 0){
    			deleteFCB((DefaultMutableTreeNode) parent.getFirstChild());
    		}
    	}
    	else{
    		for(int i = 0; i != fcb.blocks.size(); i++){
    			releaseLastBlockFree(fcb.blocks.get(i));
    		}
    	}
    	parent.removeFromParent();
    	return true;
    }
    static int moveFCB(DefaultMutableTreeNode node, DefaultMutableTreeNode parent){
    	if(node == null || parent == null){
    		return 0;
    	}
    	for(DefaultMutableTreeNode temp = parent; temp != null; temp = (DefaultMutableTreeNode) temp.getParent()){
			if(temp == node){
				return -1;
			}
    	}
    	if(((FCB)parent.getUserObject()).type == FILE){
    		parent = (DefaultMutableTreeNode) parent.getParent();
    		//return false;
    	}
    	if(node.getParent() == parent){
    		return 1;
    	}
    	FCB fcb = (FCB)node.getUserObject();
    	String name = fcb.name;
    	fcb.name = getName(parent, name);
    	node.removeFromParent();
    	parent.add(node);
    	return 1;
    }
    //文件操作实现
	static String openFile(DefaultMutableTreeNode node){
		String text = "";
    	if(node == null){
    		return null;
    	}
    	FCB fcb = (FCB)node.getUserObject();
    	if(fcb.type == FOLDER){
    		return null;
    	}
    	for(int i = 0; i != fcb.blocks.size(); i++){
    		File file = new File(blocksFolderPath + "/" + fcb.blocks.get(i));
    		try {
				@SuppressWarnings("resource")
				Scanner input = new Scanner(file);
				while(input.hasNextLine()){
					text += (input.nextLine() + "\n");
				}
			} catch (FileNotFoundException e) {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
    	}
    	return text;
    }
    static int storeFile(String text, DefaultMutableTreeNode node){
    	if(text == null || node == null){
    		return 0;
    	}
    	FCB fcb = (FCB)node.getUserObject();
    	if(fcb.type == FOLDER){
    		return 0;
    	}
    	int sizeCounter = 0;
    	int pageCounter;
    	String temp;
    	@SuppressWarnings("resource")
		Scanner input = new Scanner(text);
    	//System.out.println("*" + text.getText());
    	for(;input.hasNextLine();){
    		input.nextLine();
    		sizeCounter++;
    	}
    	//计算所需页数
    	if(sizeCounter == 0){
    		pageCounter = 0;
    	}
    	else{
    		pageCounter = (sizeCounter - 1)/BLOCK_SIZE + 1;
    	}
    	//按所需页数修改FCB中存储器页数
    	if(pageCounter < fcb.blocks.size()){
    		while(pageCounter < fcb.blocks.size()){
    			releaseLastBlockFree(fcb.blocks.get(pageCounter));
    			fcb.blocks.remove(pageCounter);
    		}
    	}
    	else if(pageCounter > fcb.blocks.size()){
    		if(countBlockFree + fcb.blocks.size() < pageCounter){
    			return -1;
    		}
    		while(pageCounter > fcb.blocks.size()){
    			int freeAddress = fetchFirstBlockFree();
    			fcb.blocks.add(new Integer(freeAddress));
    		}
    	}
    	//保存文件
    	input = new Scanner(text);
    	//System.out.println("*" + text.getText());
    	BufferedWriter bufferedwriter;
    	for(int i = 0; i != pageCounter && input.hasNextLine(); i++){
			//System.out.println(i);
    		try {
				bufferedwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(blocksFolderPath + "/" + fcb.blocks.get(i)))));
	    		for(int j = 0; j != BLOCK_SIZE && input.hasNextLine(); j++){
	    			temp = input.nextLine();
	    			try {
	    				//System.out.println(temp);
						bufferedwriter.write(temp);
		    			bufferedwriter.newLine();
		    			//System.out.println("行写入成功");
					} catch (IOException e) {
						e.printStackTrace();
		    			//System.out.println("行写入失败");
						return -2;
					}
	    		}
	    		try {
					bufferedwriter.close();
	    			//System.out.println("文件写入成功");
				} catch (IOException e) {
					e.printStackTrace();
	    			//System.out.println("文件写入失败");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
    			//System.out.println("未找到文件");
				return -2;
			}
    	}
    	return 1;
    }
    static int copyFile(DefaultMutableTreeNode node, DefaultMutableTreeNode parent){
    	if(node == null){
    		return 0;
    	}
    	if(parent == null){
    		parent = (DefaultMutableTreeNode) node.getParent();
    	}
    	if(parent == null){
    		return 0;
    	}
    	if(((FCB)parent.getUserObject()).type == FILE){
    		parent = (DefaultMutableTreeNode) parent.getParent();
    		//return 0;
    	}
    	FCB fcb = (FCB)node.getUserObject();

		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new FCB(getName(parent, fcb.name), fcb.type));
    	parent.add(newNode);
    	if(storeFile(openFile(node), newNode) == -1){
    		newNode.removeFromParent();
    		return -1;
    	}
    	if(fcb.type == FILE){
        	return 1;
    	}
    	else{
    		for(DefaultMutableTreeNode temp = parent; temp != null; temp = (DefaultMutableTreeNode) temp.getParent()){
    			if(temp == node){
    				newNode.removeFromParent();
    				return -2;
    			}
    		}
    		for(int i = 0; i != node.getChildCount(); i++){
    			if(copyFile((DefaultMutableTreeNode) node.getChildAt(i), newNode) == -1){
    				return -1;
    			}
    		}
    	}
    	return 1;
    }
    static boolean isBlockFree(int index){
    	return listBlockFree.contains(index);
    }
}
