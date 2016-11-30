package osmanagement.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import osmanagement.main.FunctionItem;

public class DocSimulator implements FunctionItem {
	private static final int WID1 = 200; 
	private static final int WID2 = WID1 + 230;
	private static final int WID3 = WID2 + 230;
	private static final int WID_B = 100;
	private static final int WID_GAP = 10;
	private static final int WID_F = WID_B + WID_GAP;
	private static final int HEI1 = 300;
	private static final int HEI2 = HEI1 + 100;
	private static final int HEI3 = HEI2 + 100;
	private static final int HEI_B = 20;
	private static final int HEI_GAP = 5;
	private static final int HEI_F = HEI_B + HEI_GAP;
	private boolean flagIsShear;
	private boolean isHelpOpened = false;
	private boolean isSystemAttributesOpened = false;
	private DefaultMutableTreeNode shearPlate;
	private DefaultMutableTreeNode currentSelected;
	private JFrame frame;
	private JTree tree;
	private JScrollPane scrollTree;
	private JScrollPane []scrollTextPad = new JScrollPane[2];
	private DefaultMutableTreeNode root;
	private JTextArea []textPad = new JTextArea[2];
	private JTextField textName;
	private JTextField []output = new JTextField[2];
	private JLabel []label = new JLabel[1];
	private JButton buttonCreateFile;
	private JButton buttonCreateFolder;
	private JButton buttonRename;
	private JButton buttonShear;
	private JButton buttonPaste;
	private JButton buttonCopy;
	private JButton buttonOpenFile;
	private JButton buttonDelete;
	private JButton buttonSystemAttributes;
	private JButton buttonHelp;
	private JButton buttonFormat;
	
	public void init(){
		frame = new JFrame("osManagement_document");
		frame.setSize(WID3 + 20, HEI3 + 40);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setVisible(true);		
		
		root = FCB.loadFCB("blocks", FCB.getFCBPath());
		tree = new JTree(root);
		//frame.add(tree);
		tree.setVisible(true);
		//tree.setBounds(10, 10, 200, 200);
		
		scrollTree = new JScrollPane(tree);
		tree.setCellRenderer(new MyDefaultTreeCellRenderer());
		frame.add(scrollTree);
		scrollTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);    //水平滚动条根据需要才显示
		scrollTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);       //垂直滚动条根据需要才显示
		scrollTree.setPreferredSize(new Dimension(300, 200));
		scrollTree.setBounds(0, 0, WID1, HEI3);
		
		tree.addTreeSelectionListener((TreeSelectionEvent e)-> {
            	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);            	
                currentSelected = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                updateText(textPad[0], null);
                //System.out.println("你选择了：" + fcb.toString()); 
            }
        );
		
		for(int i = 0; i != 2; i++){
			textPad[i] = new JTextArea();
			scrollTextPad[i] = new JScrollPane(textPad[i]);
			frame.add(scrollTextPad[i]);
		}
		scrollTextPad[0].setBounds(WID2, 0, WID3 - WID2, HEI1);
		scrollTextPad[1].setBounds(WID2, HEI1, WID3 - WID2, HEI2 - HEI1);
		for(int i = 0; i != 2; i++){
			scrollTextPad[i].setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);       //垂直滚动条根据需要才显示
			scrollTextPad[i].setPreferredSize(new Dimension(300, 200));
			textPad[i].setLineWrap(true);
			textPad[i].setEditable(false);
			textPad[i].setBackground(Color.lightGray);
		}
		
		for(int i = 0; i != 2; i++){
			output[i] = new JTextField();
			frame.add(output[i]);
			output[i].setEditable(false);
			output[i].setBackground(Color.lightGray);
		}
		output[0].setBounds(WID1 + WID_GAP, 0, WID_B * 2 + WID_GAP, HEI_B);
		output[1].setBounds(WID1 + WID_GAP, HEI_F * 5, WID_B * 2 + WID_GAP, HEI_B);
		output[1].setText("剪切板中无项目");
		textName = new JTextField();
		frame.add(textName);
		textName.setBounds(WID1 + WID_GAP, HEI_F * 2, WID_B * 2 + WID_GAP, HEI_B);
		
		label[0] = new JLabel("请在这里输入文件或文件夹名:");
		frame.add(label[0]);
		label[0].setBounds(WID1 + WID_GAP, HEI_F, WID_B * 2 + WID_GAP, HEI_B);
		
		buttonCreateFile = new JButton("新建文件");
		frame.add(buttonCreateFile);
		buttonCreateFile.setBounds(WID1 + WID_GAP, HEI_F * 3,WID_B, HEI_B);
		buttonCreateFile.addActionListener((ActionEvent e)->{
				String tempName = getLegalNameFromText(textName);
				if(tempName == null){
					return;
				}
				if(tempName.equals("")){
					tempName = "new";
				}
				DefaultMutableTreeNode node = FCB.createFCB(currentSelected, tempName, FCB.FILE);
				update();
				if(node == null){
					output[0].setText("不能在文件下新建文件");
				}else{
					output[0].setText("文件"+ node +"创建成功!");
				}
			});

		buttonCreateFolder = new JButton("新建文件夹");
		frame.add(buttonCreateFolder);
		buttonCreateFolder.setBounds(WID1 + WID_GAP, HEI_F * 4, WID_B, HEI_B);
		buttonCreateFolder.addActionListener((ActionEvent e)->{
				String tempName = getLegalNameFromText(textName);
				if(tempName == null){
					return;
				}
				if(tempName.equals("")){
					tempName = "new";
				}
				DefaultMutableTreeNode node = FCB.createFCB(currentSelected, tempName, FCB.FOLDER);
				update();
				if(node == null){
					output[0].setText("不能在文件下新建文件夹");
				}else{
					output[0].setText("文件夹"+ node +"创建成功!");
				}
			});

		buttonRename = new JButton("重命名");
		frame.add(buttonRename);
		buttonRename.setBounds(WID1 + WID_GAP + WID_F, HEI_F * 3, WID_B, HEI_F + HEI_B);
		buttonRename.addActionListener((ActionEvent e)->{
				String tempName = getLegalNameFromText(textName);
				if(tempName == null){
					return;
				}
				if(tempName.equals("")){
					output[0].setText("请输入修改后的命名");
					return;
				}
				int exi = JOptionPane.showConfirmDialog (null, "您确定要将该文件重命名为\"" + tempName + "\"?", "友情提示", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (exi == JOptionPane.YES_OPTION){
    			((FCB)currentSelected.getUserObject()).name = FCB.getName((DefaultMutableTreeNode) currentSelected.getParent(), tempName);
    			update();
                }else{
                    return;
                }
			});
			
		buttonShear = new JButton("剪切");
		frame.add(buttonShear);
		buttonShear.setBounds(WID1 + WID_GAP, HEI_F * 6, WID_B, HEI_B);
		buttonShear.addActionListener((ActionEvent e)->{ 
				shearPlate = currentSelected;
				flagIsShear = true;
				output[1].setText("已选中,可移动: " + shearPlate);
			});
		
		buttonCopy = new JButton("复制");
		frame.add(buttonCopy);
		buttonCopy.setBounds(WID1 + WID_GAP, HEI_F * 7, WID_B , HEI_B);
		buttonCopy.addActionListener((ActionEvent e)->{
				shearPlate = currentSelected;
				flagIsShear = false;
				output[1].setText("已选中,可复制: " + shearPlate);
			});
		
		buttonPaste = new JButton("粘贴");
		frame.add(buttonPaste);
		buttonPaste.setBounds(WID1 + WID_GAP + WID_F, HEI_F * 6, WID_B, HEI_F + HEI_B);
		buttonPaste.addActionListener((ActionEvent e)->{ 
				if(currentSelected == null){
					return;
				}
				if(shearPlate == null){
					output[0].setText("剪贴板中无项目");
					return;
				}
				if(flagIsShear){
					int flag = FCB.moveFCB(shearPlate, currentSelected);
					update();
					if(flag == -1){
						output[0].setText("剪贴失败,不可将文件移动到其子目录");
					}else{
						output[0].setText("移动成功");
					}
				}
				else{
					int flag = FCB.copyFile(shearPlate, currentSelected);
					update();
					if(flag == -1){
						output[0].setText("复制失败,磁盘空间已满");
					}else if(flag == -2){
						output[0].setText("复制失败,不可将文件复制到其子目录");
					}else{
						output[0].setText("复制成功");
					}
				}
				output[1].setText("剪贴板中无项目");
				shearPlate = null;
			});
	
		buttonOpenFile = new JButton("打开文件");
		frame.add(buttonOpenFile);
		buttonOpenFile.setBounds(WID1 + WID_GAP, HEI_F * 8, WID_B, HEI_B);
		buttonOpenFile.addActionListener((ActionEvent e)->{
				openFileInFrame(currentSelected);
			});
		
		buttonDelete = new JButton("删除");
		frame.add(buttonDelete);
		buttonDelete.setBounds(WID1 + WID_GAP + WID_F, HEI_F * 8, WID_B, HEI_B);
		buttonDelete.addActionListener((ActionEvent e)->{
				if(currentSelected == root){
					output[0].setText("根目录请谨慎操作!");
					JOptionPane.showConfirmDialog (null, "根目录请谨慎操作!如有必要请点击格式化!", "友情提示", JOptionPane.YES_OPTION, JOptionPane.WARNING_MESSAGE);
					return;
				}
				int exi = JOptionPane.showConfirmDialog (null, "您确定要删除该项?", "友情提示", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (exi == JOptionPane.YES_OPTION){
                	boolean flag = FCB.deleteFCB(currentSelected);
                	if(flag){
                    	currentSelected = null;
        				update();
                	}
                	else{
                		output[0].setText("该文件已打开,请先关闭文件");
                	}
                }else{
                    return;
                }				
			});
		
		buttonHelp = new JButton("关于");
		frame.add(buttonHelp);
		buttonHelp.setBounds(WID1 + WID_GAP + WID_F, HEI2 + HEI_F * 2, WID_B, HEI_B);
		buttonHelp.addActionListener((ActionEvent e)->{
				openHelpFrame();
			});
		
		buttonSystemAttributes = new JButton("系统属性");
		frame.add(buttonSystemAttributes);
		buttonSystemAttributes.setBounds(WID2 + WID_GAP, HEI2 + HEI_F * 2, WID_B, HEI_B);
		buttonSystemAttributes.addActionListener((ActionEvent e)->{
				openSystemAttributes();
			});
		
		buttonFormat = new JButton("格式化");
		frame.add(buttonFormat);
		buttonFormat.setBounds(WID2 + WID_GAP + WID_F, HEI2 + HEI_F * 2, WID_B, HEI_B);
		buttonFormat.addActionListener((ActionEvent e)->{
				int exi = JOptionPane.showConfirmDialog (null, "您确定要进行格式化（操作后无法恢复）", "友情提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (exi == JOptionPane.YES_OPTION){
                	exi = JOptionPane.showConfirmDialog (null, "请再次确认!格式化操作无法恢复,请谨慎操作!", "友情提示", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (exi == JOptionPane.YES_OPTION){
        				FCB.format(root);
        				update();
                    }else{
                        return;
                    }
                }else{
                    return;
                }
			});
		
		update();
		//FCB.storeFCB(root, "FCB.txt");
	}
	
	private int updateText(JTextArea text1, JTextArea text2){
		if (text1 != null){
        	text1.setText("");
        	if(currentSelected != null){
    	        FCB fcb = (FCB)currentSelected.getUserObject();
    	        if(fcb.getType() == FCB.FOLDER){
    	        	text1.append("文件夹名:\t" + fcb.name + "\n");
    	        }else{
    	        	text1.append("文件名:\t" + fcb.name + "\n");
    	        }
    	        text1.append("路径:\t" + FCB.getParentPath(currentSelected) + "\n");
    	        if(fcb.getType() == FCB.FOLDER){
    	            text1.append("类型:\t" + "文件夹" + "\n");
    	        }else{
    	            text1.append("类型:\t" + "文件" + "\n");
    	            if(fcb.isOpened){
        	            text1.append("打开状态:\t" + "已打开" + "\n");
    	            }else{
        	            text1.append("打开状态:\t" + "未打开" + "\n");
    	            }
    	            text1.append("文件大小:\t" + fcb.blocks.size() + "KB\n");
    	            text1.append("存储位置:\n");
    	            for(int i = 0; i != fcb.blocks.size(); i++){
    	            	text1.append(fcb.blocks.get(i) + ", ");
    	            }
    	            text1.append("\n");
    	        }
        	}
        }
		if(text2 != null){
			text2.setText("");
			text2.append("磁盘使用率:\t" + (FCB.VOLUMN - FCB.countBlockFree) + "/" + FCB.VOLUMN + "\n");
		}
		return 1;
	}	
	private int update(){
		FCB.storeFCB(root, FCB.getFCBPath());
		tree.updateUI();
		updateText(textPad[0], textPad[1]);
		return 1;
	}
	private String getLegalNameFromText(JTextField text){
		String name = text.getText();
		text.setText("");
		return getLegalName(name);
	}
	private String getLegalName(String name){
		if(name.indexOf(' ') != -1 || name.indexOf('\t') != -1 || name.indexOf('\r') != -1 || name.indexOf('\n') != -1 
				|| name.indexOf('\\') != -1 || name.indexOf('/') != -1 || name.indexOf('~') != -1 || name.indexOf('!') != -1
				 || name.indexOf('@') != -1 || name.indexOf('#') != -1 || name.indexOf('$') != -1 || name.indexOf('%') != -1
				 || name.indexOf('^') != -1 || name.indexOf('&') != -1 || name.indexOf('?') != -1){
			output[0].setText("该命名不合法");
			return null;
		}
		return name;
	}
	private int openFileInFrame(DefaultMutableTreeNode node){
		if(node == null){
    		output[0].setText("请选中要操作的文件");
			return 0;
		}
		FCB fcb = (FCB)node.getUserObject();
    	if(fcb.getType() == FCB.FOLDER){
    		output[0].setText("只可打开文件");
    		return -1;
    	}
    	if(fcb.isOpened){
    		output[0].setText("文件\"" + fcb + "\"已打开,禁止重复打开");
    		return -2;
    	}
    	output[0].setText("成功打开文件\"" + fcb + "\"");
    	fcb.isOpened = true;
    	updateText(textPad[0], null);
		JFrame subFrame = new JFrame("文件编辑: " + fcb.name + " 路径:" + FCB.getParentPath(node));
		subFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		subFrame.setSize(500, 400);
		subFrame.setLocation(25, 25);
		subFrame.setLayout(null);
		subFrame.setVisible(true);	

		JTextArea text = new JTextArea();
		JScrollPane scrollText = new JScrollPane(text);
		subFrame.add(scrollText);
		scrollText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);       //垂直滚动条根据需要才显示
		scrollText.setPreferredSize(new Dimension(300, 200));
		scrollText.setBounds(10, 10, 460, 300);
		text.setLineWrap(true);
		
		JButton buttonStoreFileSub = new JButton("保存文件");
		subFrame.add(buttonStoreFileSub);
		buttonStoreFileSub.setBounds(20, 320, 100, 20);
		buttonStoreFileSub.addActionListener((ActionEvent e)->{
				if(FCB.storeFile(text.getText(), node) == -1){
					output[0].setText("磁盘空间不足");
					JOptionPane.showConfirmDialog (null, "磁盘空间不足，无法保存!", "友情提示", JOptionPane.YES_OPTION, JOptionPane.WARNING_MESSAGE);
					return;
				}
				update();
			});
		
		subFrame.addWindowListener (new WindowAdapter ()
        {
            @Override
            public void windowClosing ( WindowEvent e )
            {
                int exi = JOptionPane.showConfirmDialog (null, "是否在退出时保存？？", "友情提示", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (exi == JOptionPane.YES_OPTION){
    				if(FCB.storeFile(text.getText(), node) == -1){
    					output[0].setText("磁盘空间不足");
    					JOptionPane.showConfirmDialog (null, "磁盘空间不足，无法保存!", "友情提示", JOptionPane.YES_OPTION, JOptionPane.WARNING_MESSAGE);
    					return;
    				}
    				fcb.isOpened = false;
    				update();
                    subFrame.dispose();
                }
                else if(exi == JOptionPane.NO_OPTION){
    				fcb.isOpened = false;
    				updateText(textPad[0], null);
                	subFrame.dispose();
                }
                else{
                    return;
                }
            }
        });
		text.setText(FCB.openFile(node));
		return 1;
	}
	private int openHelpFrame(){
		if(isHelpOpened){
			output[0].setText("关于窗口已打开");
			return -1;
		}
		isHelpOpened = true;
		JFrame helpFrame = new JFrame("关于");
		helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		helpFrame.setSize(300, 400);
		helpFrame.setLayout(null);
		helpFrame.setVisible(true);	
		JTextArea text = new JTextArea();
		helpFrame.add(text);
		text.setBounds(10, 10, 260, 300);
		text.setBackground(Color.lightGray);
		text.setEditable(false);
		text.setText("制作人员:\t尹屹凡\n学号:\t1452692\n课程:\t操作系统\n年级:\t软件学院2014级\n编程语言:\tJAVA\n编译环境:\teclipse-Mars\n\n\t欢迎使用!");
		helpFrame.addWindowListener (new WindowAdapter ()
        {
            @Override
            public void windowClosing ( WindowEvent e )
            {
            	isHelpOpened = false;
                helpFrame.dispose();
            }
        });
		return 1;
	}
	private int openSystemAttributes(){
		if(isSystemAttributesOpened){
			output[0].setText("系统属性窗口已打开");
			return -1;
		}
		isSystemAttributesOpened = true;
		JFrame saFrame = new JFrame("系统属性");
		saFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		saFrame.setSize(300, 400);
		saFrame.setLayout(null);
		saFrame.setVisible(true);	

		JTextArea text = new JTextArea();
		JScrollPane scrollText = new JScrollPane(text);
		saFrame.add(scrollText);
		scrollText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollText.setPreferredSize(new Dimension(300, 200));
		scrollText.setBounds(10, 10, 260, 80);
		text.setBackground(Color.lightGray);
		text.setEditable(false);
		text.setLineWrap(true);
		updateText(null, text);
		text.append("以下是硬盘具体使用情况:\n蓝色代表页块空闲，橙色代表页块已用");
		
		int size = 22;
		int border = size + 1;
		int width = 10;
		int height = 0;
		if(FCB.VOLUMN > 0){
			height = (FCB.VOLUMN - 1) / width + 1;
		}
		JPanel disk = new JPanel();
		
		JScrollPane scrollDisk = new JScrollPane(disk);
		saFrame.add(scrollDisk);
		scrollDisk.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollDisk.setPreferredSize(new Dimension(300, 200));
		scrollDisk.setBounds(10, 90, 260, 270);
		
		disk.setLayout(null);
		JPanel[] pages = new JPanel[FCB.VOLUMN];
		disk.setPreferredSize(new Dimension(border * width, border * height));
		disk.setVisible(true);
		for(int i = 0; i != height; i++){
			for(int j = 0; j != width; j++){
				if(i * width + j == FCB.VOLUMN){
					break;
				}
				pages[i * width + j] = new JPanel();
				disk.add(pages[i * width + j]);
				pages[i * width + j].setBounds(border * j, border * i, size, size);
				if(FCB.isBlockFree(i * width + j)){
					pages[i * width + j].setBackground(Color.cyan);
				}
				else{
					pages[i * width + j].setBackground(Color.orange);
				}
			}
		}		
		disk.revalidate();
		saFrame.addWindowListener (new WindowAdapter ()
        {
            @Override
            public void windowClosing ( WindowEvent e )
            {
            	isSystemAttributesOpened = false;
                saFrame.dispose();
            }
        });
		return 1;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new DocSimulator().excuteFuction();
	}

	@Override
	public void excuteFuction() {
		DocSimulator ds = new DocSimulator();
		ds.init();
	}
}
