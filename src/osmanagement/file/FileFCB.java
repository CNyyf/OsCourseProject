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

public class FileFCB extends FCB {
	ArrayList<Integer> blocks = new ArrayList<Integer>();
	
	public FileFCB(String n) {
		super(n);
	}
    boolean cDeleteFCB() {
    	for(int i = 0; i != blocks.size(); i++){
			releaseLastBlockFree(blocks.get(i));
		}
    	return true;
    }
    int cMoveFCB(DefaultMutableTreeNode node) {
    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) theNode.getParent();
    	return ((FCB)parent.getUserObject()).cMoveFCB(node);
    }
    String cOpenFile() {
    	String text = "";
    	for(int i = 0; i != blocks.size(); i++){
    		File file = new File(blocksFolderPath + "/" + blocks.get(i));
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
    int cStoreFile(String text) {
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
    	if(pageCounter < blocks.size()){
    		while(pageCounter < blocks.size()){
    			releaseLastBlockFree(blocks.get(pageCounter));
    			blocks.remove(pageCounter);
    		}
    	}
    	else if(pageCounter > blocks.size()){
    		if(countBlockFree + blocks.size() < pageCounter){
    			return -1;
    		}
    		while(pageCounter > blocks.size()){
    			int freeAddress = fetchFirstBlockFree();
    			blocks.add(new Integer(freeAddress));
    		}
    	}
    	//保存文件
    	input = new Scanner(text);
    	//System.out.println("*" + text.getText());
    	BufferedWriter bufferedwriter;
    	for(int i = 0; i != pageCounter && input.hasNextLine(); i++){
			//System.out.println(i);
    		try {
				bufferedwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(blocksFolderPath + "/" + blocks.get(i)))));
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
    int cCopyFCB(DefaultMutableTreeNode node) {
    	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) theNode.getParent();
    	return ((FCB)parent.getUserObject()).cCopyFCB(node);
    }
    int cDeepCopyInto(DefaultMutableTreeNode parent, DefaultMutableTreeNode newNode) {
    	if(storeFile(openFile(theNode), newNode) == -1){
    		newNode.removeFromParent();
    		((FCB)newNode.getUserObject()).removeFromParent();
    		return -1;
    	}
    	return 1;
    }
    public int getType(){
    	return FILE;
    }
    ArrayList<Integer> blocks(){
    	return blocks;
    }
}
