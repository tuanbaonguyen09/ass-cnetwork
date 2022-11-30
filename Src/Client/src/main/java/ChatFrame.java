import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

public class ChatFrame extends JFrame {
	private JButton btnFile;
	private JButton btnSend;
	private JScrollPane chatPanel;
	private JLabel lbReceiver = new JLabel(" ");
	private JPanel contentPane;
	private JTextField txtMessage;
	private JTextPane chatWindow;
	JComboBox<String> onlineUsers = new JComboBox<String>();
	private HashMap<String, JTextPane> chatWindows = new HashMap<>();
	private String username;
	private DataInputStream dis;
	private DataOutputStream dos;
	Thread receiver;

	private void autoScroll() {
		chatPanel.getVerticalScrollBar().setValue(chatPanel.getVerticalScrollBar().getMaximum());
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	private void newFile(String username, String filename, byte[] file, Boolean yourMessage) {

		StyledDocument doc;
		String window = null;
		if (username.equals(this.username)) {
			window = lbReceiver.getText();
		} else {
			window = username;
		}
		doc = chatWindows.get(window).getStyledDocument();
		
		Style userStyle = doc.getStyle("User style");
		if (userStyle == null) {
			userStyle = doc.addStyle("User style", null);
			StyleConstants.setBold(userStyle, true);
		}
		
		if (yourMessage == true) {
	    	StyleConstants.setForeground(userStyle, Color.BLACK);
	    } else {
	    	StyleConstants.setForeground(userStyle, Color.BLACK);
	    }

	    try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}
		
	    Style linkStyle = doc.getStyle("Link style");
	    if (linkStyle == null) {
	    	linkStyle = doc.addStyle("Link style", null);
	    	StyleConstants.setForeground(linkStyle, Color.BLUE);
			StyleConstants.setUnderline(linkStyle, true);
			StyleConstants.setBold(linkStyle, true);
			linkStyle.addAttribute("link", new HyberlinkListener(filename, file));
	    }
	    
	    if (chatWindows.get(window).getMouseListeners() != null) {
	    	// Tạo MouseListener cho các đường dẫn tải về file
			chatWindows.get(window).addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent evt){
					Element element = doc.getCharacterElement(chatWindow.viewToModel2D(evt.getPoint()));
		            AttributeSet as = element.getAttributes();
		            HyberlinkListener listener = (HyberlinkListener)as.getAttribute("link");
		            if(listener != null)
		            {
		                listener.execute();
		            }
		        }
				
				@Override
				public void mousePressed(MouseEvent e) {
				}
	
				@Override
				public void mouseReleased(MouseEvent e) {
				}
	
				@Override
				public void mouseEntered(MouseEvent e) {
				}
	
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
			});
		}
	    // In ra đường dẫn tải file
		try {
			doc.insertString(doc.getLength(),"<" + filename + ">", linkStyle);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
		// Xuống dòng
		try {
			doc.insertString(doc.getLength(), "\n", userStyle);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
		autoScroll();
	}
	
	// Đẩy tin nhắn lên màn hình hiển thị
	private void newMessage(String username, String message, Boolean yourMessage) {

		StyledDocument doc;
		if (username.equals(this.username)) {
			doc = chatWindows.get(lbReceiver.getText()).getStyledDocument();
		} else {
			doc = chatWindows.get(username).getStyledDocument();
		}
		
		Style userStyle = doc.getStyle("User style");
		if (userStyle == null) {
			userStyle = doc.addStyle("User style", null);
			StyleConstants.setBold(userStyle, true);
		}
		
		if (yourMessage == true) {
	    	StyleConstants.setForeground(userStyle, Color.BLACK);
	    } else {
	    	StyleConstants.setForeground(userStyle, Color.BLACK);
	    }
	    
		// In ra tên người gửi
	    try { doc.insertString(doc.getLength(), username + ": ", userStyle); }
        catch (BadLocationException e){}
	    
	    Style messageStyle = doc.getStyle("Message style");
		if (messageStyle == null) {
			messageStyle = doc.addStyle("Message style", null);
		    StyleConstants.setForeground(messageStyle, Color.BLACK);
		    StyleConstants.setBold(messageStyle, false);
		}
	   
		// In ra nội dung tin nhắn
	    try { doc.insertString(doc.getLength(), message + "\n",messageStyle); }
        catch (BadLocationException e){}
	    
	    autoScroll();
	}
	
	public ChatFrame(String username, DataInputStream dis, DataOutputStream dos) {
		setTitle("Chat Room");	
		this.username = username;
		this.dis = dis;
		this.dos = dos;
		receiver = new Thread(new Receiver(dis));
		receiver.start();
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 586, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setBackground(new Color(255, 253, 250));
		setContentPane(contentPane);
		JPanel header = new JPanel();
		header.setBackground(new Color(255, 253, 250));
		
		txtMessage = new JTextField();
		txtMessage.setEnabled(false);
		txtMessage.setColumns(10);
		
		btnSend = new JButton("Send");
		btnSend.setFont(new Font("Arial", Font.BOLD, 10));
		btnSend.setEnabled(false);
		
		chatPanel = new JScrollPane();
		chatPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel leftPanel = new JPanel();
		leftPanel.setBackground(new Color(255, 253, 250));
		
		btnFile = new JButton("File");
		btnFile.setFont(new Font("Arial", Font.BOLD, 10));
		btnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// Hiển thị hộp thoại cho người dùng chọn file để gửi
				JFileChooser fileChooser = new JFileChooser();
				int rVal = fileChooser.showOpenDialog(contentPane.getParent());
				if (rVal == JFileChooser.APPROVE_OPTION) {
					byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
					BufferedInputStream bis;
					try {
						bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
						// Đọc file vào biến selectedFile
						bis.read(selectedFile, 0, selectedFile.length);
						dos.writeUTF("File");
						dos.writeUTF(lbReceiver.getText());
						dos.writeUTF(fileChooser.getSelectedFile().getName());
						dos.writeUTF(String.valueOf(selectedFile.length));
						int size = selectedFile.length;
						int bufferSize = 2048;
						int offset = 0;
						
						// Lần lượt gửi cho server từng buffer cho đến khi hết file
						while (size > 0) {
							dos.write(selectedFile, offset, Math.min(size, bufferSize));
							offset += Math.min(size, bufferSize);
							size -= bufferSize;
						} 
						dos.flush();
						bis.close();
						// In ra màn hình file
						newFile(username, fileChooser.getSelectedFile().getName(), selectedFile, true);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnFile.setEnabled(false);

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(header, GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(leftPanel, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(txtMessage, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnFile, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
						.addComponent(chatPanel, GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(header, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(chatPanel, GroupLayout.DEFAULT_SIZE, 286, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnSend, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnFile, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtMessage, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)))
						.addComponent(leftPanel, GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)))
		);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 253, 250));
		JLabel lblNewLabel_1 = new JLabel("Online User");
		lblNewLabel_1.setFont(new Font("Serif", Font.BOLD, 12));
		GroupLayout gl_leftPanel = new GroupLayout(leftPanel);
		gl_leftPanel.setHorizontalGroup(
			gl_leftPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_leftPanel.createSequentialGroup()
					.addGap(25)
					.addGap(25))
				.addGroup(gl_leftPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
					.addContainerGap())
				.addGroup(gl_leftPanel.createSequentialGroup()
					.addGap(28)
					.addComponent(lblNewLabel_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(29))
				.addGroup(Alignment.TRAILING, gl_leftPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(onlineUsers, 0, 101, Short.MAX_VALUE)
					.addContainerGap())
		);
		onlineUsers.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					lbReceiver.setText((String) onlineUsers.getSelectedItem());
					if (chatWindow != chatWindows.get(lbReceiver.getText())) {
						txtMessage.setText("");
						chatWindow = chatWindows.get(lbReceiver.getText());
						chatPanel.setViewportView(chatWindow);
						chatPanel.validate();
					}
					
					if (lbReceiver.getText().isBlank()) {
						btnSend.setEnabled(false);
						btnFile.setEnabled(false);
						txtMessage.setEnabled(false);
					} else {
						btnSend.setEnabled(true);
						btnFile.setEnabled(true);
						txtMessage.setEnabled(true);
					}
				}

			}
		});
		
		gl_leftPanel.setVerticalGroup(
			gl_leftPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_leftPanel.createSequentialGroup()
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
					.addComponent(lblNewLabel_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(onlineUsers, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(104, Short.MAX_VALUE))
		);
		JLabel lbwelcome = new JLabel("Welcome");
		lbwelcome.setFont(new Font("Arial", Font.BOLD, 12));
		JLabel lbUsername = new JLabel(this.username);
		lbUsername.setFont(new Font("Arial", Font.BOLD, 12));
		panel.add(lbwelcome);
		panel.add(lbUsername);
		leftPanel.setLayout(gl_leftPanel);
		
		JLabel headerContent = new JLabel("CHAT ROOM");
		headerContent.setFont(new Font("Serif", Font.BOLD, 20));
		header.add(headerContent);
		
		JPanel usernamePanel = new JPanel();
		usernamePanel.setBackground(new Color(255, 253, 250));
		chatPanel.setColumnHeaderView(usernamePanel);
		
		lbReceiver.setFont(new Font("Serif", Font.BOLD, 16));
		usernamePanel.add(lbReceiver);
		
		chatWindows.put(" ", new JTextPane());
		chatWindow = chatWindows.get(" ");
		chatWindow.setFont(new Font("Serif", Font.PLAIN, 14));
		chatWindow.setEditable(false);
		chatPanel.setViewportView(chatWindow);
		contentPane.setLayout(gl_contentPane);

		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (txtMessage.getText().isBlank() || lbReceiver.getText().isBlank()) {
					btnSend.setEnabled(false);
				} else {
					btnSend.setEnabled(true);
				}
			}
		});

		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					dos.writeUTF("Text");
					dos.writeUTF(lbReceiver.getText());
					dos.writeUTF(txtMessage.getText());
					dos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
					newMessage("ERROR" , "Error!" , true);
				}
				// In ra tin nhắn lên màn hình chat với người nhận
				newMessage(username , txtMessage.getText() , true);
				txtMessage.setText("");
			}
		});
		
		this.getRootPane().setDefaultButton(btnSend);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				
				try {
					dos.writeUTF("Log out");
					dos.flush();
					
					try {
						receiver.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					if (dos != null) {
						dos.close();
					}
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
	}
	//Luồng đọc tin nhắn
	class Receiver implements Runnable{
		private DataInputStream dis;
		public Receiver(DataInputStream dis) {
			this.dis = dis;
		}
		@Override
		public void run() {
			try {		
				while (true) {
					// Chờ tin nhắn từ server
					String method = dis.readUTF();
					if (method.equals("Text")) {
						//Yêu cầu : Nhận tin nhắn
						String sender =	dis.readUTF();
						String message = dis.readUTF();
						// Display tin nhắn lên màn hình
						newMessage(sender, message, false);
					}
					
					else if (method.equals("File")) {
						// Yêu cầu: Nhận file
						String sender = dis.readUTF();
						String filename = dis.readUTF();
						int size = Integer.parseInt(dis.readUTF());
						int bufferSize = 2048;
						byte[] buffer = new byte[bufferSize];
						ByteArrayOutputStream file = new ByteArrayOutputStream();
						
						while (size > 0) {
							dis.read(buffer, 0, Math.min(bufferSize, size));
							file.write(buffer, 0, Math.min(bufferSize, size));
							size -= bufferSize;
						}
						
						// In ra màn hình file đó
						newFile(sender, filename, file.toByteArray(), false);
					}
					else if (method.equals("Online users")) {
						// Yêu cầu : Danh sách online
						String[] users = dis.readUTF().split(",");
						onlineUsers.removeAllItems();
						String chatting = lbReceiver.getText();

						boolean isChattingOnline = false;
						for (String user: users) {
							if (user.equals(username) == false) {
								// Update danh sách người dùng online
								onlineUsers.addItem(user);
								if (chatWindows.get(user) == null) {
									JTextPane temp = new JTextPane();
									temp.setFont(new Font("Serif", Font.PLAIN, 13));
									temp.setEditable(false);
									chatWindows.put(user, temp);
								}
							}
							if (chatting.equals(user)) {
								isChattingOnline = true;
							}
						}
						if (isChattingOnline == false) {
							// Khi người dùng kia mất kết nối
							onlineUsers.setSelectedItem(" ");
							JOptionPane.showMessageDialog(null, chatting + " is offline!");
						} else {
							onlineUsers.setSelectedItem(chatting);
						}

						onlineUsers.validate();
					}
					else if (method.equals("Safe to leave")) {
						// Thông báo có thể thoát
						break;
					}
				}
				
			} catch(IOException ex) {
				System.err.println(ex);
			} finally {
				try {
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class HyberlinkListener extends AbstractAction {
		String filename;
		byte[] file;
		
		public HyberlinkListener(String filename, byte[] file) {
			this.filename = filename;
			this.file = Arrays.copyOf(file, file.length);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			execute();
		}
		
		public void execute() {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File(filename));
			int rVal = fileChooser.showSaveDialog(contentPane.getParent());
			if (rVal == JFileChooser.APPROVE_OPTION) {
				File saveFile = fileChooser.getSelectedFile();
				BufferedOutputStream bos = null;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(saveFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				// Kiểm tra người dùng muốn mở fie
				int nextAction = JOptionPane.showConfirmDialog(null, "Saved file to " + saveFile.getAbsolutePath() + "\nDo you want to open this file?", "Successful", JOptionPane.YES_NO_OPTION);
				if (nextAction == JOptionPane.YES_OPTION) {
					try {
						Desktop.getDesktop().open(saveFile);
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
				if (bos != null) {
					try {
						bos.write(this.file);
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		    }
		}
	}
	
}
