import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout.*;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.awt.*;

public class LoginFrame extends JFrame {
	private String host = "localhost";
	private int port = 9999;
	private JPanel contentPane;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private Socket socket;
	private DataInputStream is;
	private DataOutputStream os;
	private String username;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginFrame frame = new LoginFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	// Login Frame
	public LoginFrame() {
		setTitle("Login Window");
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 410, 290);
		//Main Pane
		contentPane = new JPanel();
		contentPane.setBackground(new Color(255, 253, 250));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		//Header 
		JPanel headerPanel = new JPanel();
		headerPanel.setBackground(new Color(255, 253, 250));
		//User label
		JLabel lbUsername = new JLabel("Username");
		lbUsername.setFont(new Font("Serif", Font.PLAIN, 15));
		//Password label
		JLabel lbPassword = new JLabel("Password");
		lbPassword.setFont(new Font("Serif", Font.PLAIN, 15));
		//Text label
		txtUsername = new JTextField();
		txtUsername.setColumns(10);
		txtPassword = new JPasswordField();
		//Button label
		JPanel buttons = new JPanel();
		buttons.setBackground(new Color(255, 253, 250));
		JPanel notificationContainer = new JPanel();
		notificationContainer.setBackground(new Color(255, 253, 250));

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(headerPanel, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(70)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lbPassword)
							.addGap(20)
							.addComponent(txtPassword, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lbUsername)
							.addGap(18)
							.addComponent(txtUsername, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)))
					.addGap(70))
				.addComponent(notificationContainer, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
				.addComponent(buttons, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(20)
					.addComponent(headerPanel, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
					.addGap(30)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lbUsername)
						.addComponent(txtUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(30)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lbPassword))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(notificationContainer, GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(buttons, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
					.addGap(20))
		);
		
		JLabel notification = new JLabel("");
		notification.setForeground(Color.RED);
		notification.setFont(new Font("Serif", Font.PLAIN, 12));
		notificationContainer.add(notification);
		
		JButton login = new JButton("Log in");
		login.setFont(new Font("Arial", Font.PLAIN, 14));
		JButton signup = new JButton("Sign up");
		signup.setFont(new Font("Arial", Font.PLAIN, 14));
		login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String response = Login(txtUsername.getText(), String.copyValueOf(txtPassword.getPassword()));
				// đăng nhập thành công thì server sẽ trả về  chuỗi "Log in successful"
				if ( response.equals("Log in successful") ) {
					username = txtUsername.getText();
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								ChatFrame frame = new ChatFrame(username, is, os);
								frame.setVisible(true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					dispose();
				} else {
					login.setEnabled(false);
					signup.setEnabled(false);
					txtPassword.setText("");
					notification.setText(response);
				}
			}
		});
		login.setEnabled(false);
		buttons.add(login);
		
		signup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPasswordField confirm = new JPasswordField();
				// Hiển thị hộp thoại xác nhận password
			    int action = JOptionPane.showConfirmDialog(null, confirm,"Confirm your password",JOptionPane.OK_CANCEL_OPTION);
			    if (action == JOptionPane.OK_OPTION) {
			    	if (String.copyValueOf(confirm.getPassword()).equals(String.copyValueOf(txtPassword.getPassword()))) {
			    		String response = Signup(txtUsername.getText(), String.copyValueOf(txtPassword.getPassword()));
					
			    		// đăng ký thành công thì server sẽ trả về  chuỗi "Log in successful"
						if ( response.equals("Sign up successful") ) {
							username = txtUsername.getText();
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									try {
										// In ra thông báo đăng kí thành công
										JOptionPane.showConfirmDialog(null, "Sign up successful", "Notifcation", JOptionPane.DEFAULT_OPTION);
										ChatFrame frame = new ChatFrame(username, is, os);
										frame.setVisible(true);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
							dispose();
						} else {
							login.setEnabled(false);
							signup.setEnabled(false);
							txtPassword.setText("");
							notification.setText(response);
						}
					} else {
			    		notification.setText("Confirm password does not match");
			    	}
			    }
			}
		});
		signup.setEnabled(false);
		buttons.add(signup);
		JLabel headerContent = new JLabel("LOGIN");
		headerContent.setFont(new Font("Serif", Font.BOLD,30));
		headerPanel.add(headerContent);
		contentPane.setLayout(gl_contentPane);
		txtUsername.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (txtUsername.getText().isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank()) {
					login.setEnabled(false);
					signup.setEnabled(false);
				} else {
					login.setEnabled(true);
					signup.setEnabled(true);
				}
			}
		});
		
		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (txtUsername.getText().isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank()) {
					login.setEnabled(false);
					signup.setEnabled(false);
				} else {
					login.setEnabled(true);
					signup.setEnabled(true);
				}
			}
		});
		this.getRootPane().setDefaultButton(login);
	}
	
	/**
	 * Gửi yêu cầu đăng nhập đến server
	 * Trả về kết quả phản hồi từ server
	 */
	public String Login(String username, String password) {
		try {
			connect();
			os.writeUTF("Log in");
			os.writeUTF(username);
			os.writeUTF(password);
			os.flush();
			String response = is.readUTF();
			return response;
			
		} catch (IOException e) {
			e.printStackTrace();
			return "Log in failure";
		}
	}
	
	/**
	 * Gửi yêu cầu đăng ký đến server
	 * Trả về kết quả phản hồi từ server
	 */
	public String Signup(String username, String password) {
		try {
			connect();
			os.writeUTF("Sign up");
			os.writeUTF(username);
			os.writeUTF(password);
			os.flush();
			String response = is.readUTF();
			return response;
		} catch (IOException e) {
			e.printStackTrace();
			return "Sign up failure !";
		}
	}
	
	/**
	 * Kết nối đến server
	 */
	public void connect() {
		try {
			if (socket != null) {
				socket.close();
			}
			socket = new Socket(host, port);
			this.is = new DataInputStream(socket.getInputStream());
			this.os = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public String getUsername() {
		return this.username;
	}
	

}
