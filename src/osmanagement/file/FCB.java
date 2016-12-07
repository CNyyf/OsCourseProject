package osmanagement.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.tree.DefaultMutableTreeNode;

public class FCB {
	static final int BLOCK_SIZE = 10;
	static final int VOLUMN = 64;
	static boolean []blockFree = new boolean[VOLUMN];
	static int countBlockFree = VOLUMN;
	static ArrayList<Integer> listBlockFree = new ArrayList<Integer>();
    static public final int FILE = 0;
    static public final int FOLDER = 1;
    protected static String blocksFolderPath = "blocks";
	static private String FCBPath = "blocks/FCB";
    String name;
//    ArrayList<Integer> blocks = new ArrayList<Integer>();
    boolean isOpened = false;
    public FCB(String n) {
        name = n;
    }
    public int getType(){
    	return -1;
    }
    public String toString() {
        return name;
    }
    static String getFCBPath(){
    	return FCBPath;
    }
    static boolean printBlockFree(){
    	for(int i = 0; i != VOLUMN; i++){
    		System.out.print(blockFree[i] + " ");
    		if(i % 20 == 19) System.out.println("");
    	}
    	return true;
    }
    static int fetchFirstBlockFree(){
    	if(listBlockFree.size() == 0){
    		return -1;
    	}
    	int firstBlockFree = listBlockFree.get(0);
    	listBlockFree.remove(0);
    	if(blockFree[firstBlockFree]){
    		blockFree[firstBlockFree] = false;
    		countBlockFree--;
    	}
    	return firstBlockFree;
    }
    static boolean releaseLastBlockFree(int lastBlockFree){
    	listBlockFree.add(new Integer(lastBlockFree));
    	if(!blockFree[lastBlockFree]){
    		blockFree[lastBlockFree] = true;
    		countBlockFree++;
    		return true;
    	}
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
    //change: chain method definition
    static DefaultMutableTreeNode setNode(DefaultMutableTreeNode root, String p, String n, int t){
    	DefaultMutableTreeNode parent = getNodeByPath(root, p);
    	return createFCB(parent, n ,t);
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
					bufferedwriter.write(fcb.toString() + " " + FCB.getParentPath(node) + " " + fcb.getType() + " " + fcb.blocksSize());
					for(int i = 0; i != fcb.blocksSize(); i++){
						bufferedwriter.write(" " + fcb.blocks().get(i));
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
    	for(int i = 0; i != VOLUMN; i++){
    		blockFree[i] = true;
    	}
    	countBlockFree = VOLUMN;
    	try {
			input = new Scanner(new File(FCBfilePath));
			String temp;
			Scanner tempScan = null;
			if(input.hasNextLine()){
				temp = input.nextLine();
				//System.out.println(temp);
				tempScan = new Scanner(temp);
				root = createNode(createFCB(tempScan.next(), FOLDER));
			}
			else{
				root = createNode(createFCB("root", FOLDER));
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
							if(blockFree[address]){
								fcb.blocks().add(new Integer(address));
								blockFree[address] = false;
								countBlockFree--;
							}
						}
					}
				}
			}
			for(int i = 0; i != VOLUMN; i++){
				if(blockFree[i]){
					listBlockFree.add(new Integer(i));
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found!");
			root = createNode(createFCB("root", FOLDER));
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
    static int firstBlockFree(){
    	for(int i = 0; i != VOLUMN; i++){
    		if(blockFree[i]){
    			return i;
    		}
    	}
    	return -1;
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
    	for(int i = 0; i != VOLUMN; i++){
    		blockFree[i] = true;
    	}
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
    	return ((FCB)parent.getUserObject()).cCreateFCB(n, t);
    }
    static boolean deleteFCB(DefaultMutableTreeNode parent){
    	if(parent == null){
    		return false;
    	}
    	FCB fcb = (FCB)parent.getUserObject();
    	return fcb.mainDeleteFCB();
    }
    static int moveFCB(DefaultMutableTreeNode node, DefaultMutableTreeNode parent){
    	if(node == null || parent == null){
    		return 0;
    	}
    	return ((FCB)parent.getUserObject()).mainMoveFCB(node);
    }
    //文件操作实现
	static String openFile(DefaultMutableTreeNode node){
    	if(node == null){
    		return null;
    	}
    	FCB fcb = (FCB)node.getUserObject();
    	return fcb.cOpenFile();
    }
    static int storeFile(String text, DefaultMutableTreeNode node){
    	if(text == null || node == null){
    		return 0;
    	}
    	FCB fcb = (FCB)node.getUserObject();
    	return fcb.cStoreFile(text);
    }
    static int copyFile(DefaultMutableTreeNode node, DefaultMutableTreeNode parent){
    	if(parent == null){
    		return 0;
    	}
    	FCB fcb = (FCB)parent.getUserObject();
    	return fcb.mainCopyFCB(node);
    }
    static boolean isBlockFree(int index){
    	return blockFree[index];
    }
    
    //change: new
    DefaultMutableTreeNode theNode;//the DefaultMutableTreeNode which holds this FCB object
    //factory method
    static FCB createFCB(String n, int t) {
    	switch(t) {
    	case FILE:
    		return new FileFCB(n);
    	case FOLDER:
    		return new FolderFCB(n);
    	default:
    		return null;
    	}
    }
    static DefaultMutableTreeNode createNode(FCB fcb) {
    	DefaultMutableTreeNode product = new DefaultMutableTreeNode(fcb);
    	fcb.theNode = product;
    	return product;
    }
    DefaultMutableTreeNode cCreateFCB(String n, int t){return null;}
    boolean mainDeleteFCB() {
    	//System.out.println(this);
    	if(isOpened){
    		return false;
    	}
    	if(cDeleteFCB()) {
    		theNode.removeFromParent();
    		((FCB)theNode.getUserObject()).removeFromParent();
    		return true;
    	}
    	return false;
    	
    }
    boolean cDeleteFCB() {
    	return false;
    }
    int mainMoveFCB(DefaultMutableTreeNode node) {
    	if(node == null) {
    		return 0;
    	}
    	for(DefaultMutableTreeNode temp = theNode; temp != null; temp = (DefaultMutableTreeNode) temp.getParent()){
			if(temp == node){
				return -1;
			}
    	}
    	return cMoveFCB(node);
    }
    int cMoveFCB(DefaultMutableTreeNode node) {
    	return 0;
    }
    String cOpenFile() {
    	return null;
    }
    int cStoreFile(String text) {
    	return 0;
    }
    int mainCopyFCB(DefaultMutableTreeNode node) {
    	if(node == null){
    		return 0;
    	}
    	for(DefaultMutableTreeNode temp = theNode; temp != null; temp = (DefaultMutableTreeNode) temp.getParent()){
			if(temp == node){
				return -2;
			}
    	}
    	return cCopyFCB(node);
    }
    int cCopyFCB(DefaultMutableTreeNode node) {
    	return 0;
    }
    int mainDeepCopyInto(DefaultMutableTreeNode parent) {
    	FCB fcb = createFCB(getName(parent, name), getType());
    	DefaultMutableTreeNode newNode = createNode(fcb);
    	parent.add(newNode);
    	((FCB)parent.getUserObject()).members().add(fcb);
    	return cDeepCopyInto(parent, newNode);
    }
    int cDeepCopyInto(DefaultMutableTreeNode parent, DefaultMutableTreeNode newNode) {
    	return 0;
    }
    ArrayList<Integer> blocks(){
    	return null;
    }
    int blocksSize() {
    	if(blocks() != null){
    		return blocks().size();
    	}
    	else {
    		return 0;
    	}
    }
    ArrayList<FCB> members(){
    	return null;
    }
    void removeFromParent() {
    	((FCB)((DefaultMutableTreeNode)theNode.getParent()).getUserObject()).members().remove(this);
    }
}