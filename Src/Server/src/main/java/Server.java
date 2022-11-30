import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
	private String datapath = "data\\accounts.txt";
	private Object lock;
	private ServerSocket server;
	private Socket socket;
	static ArrayList<Handler> clients = new ArrayList<Handler>();

	
	/**
	 * Tải lên danh sách tài khoản từ file
	 */
	private void loadAccounts() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(datapath), "utf8"));
			
			String info = br.readLine();
			while (info != null && !(info.isEmpty())) {
				clients.add(new Handler(info.split(",")[0], info.split(",")[1], false, lock));
				info = br.readLine();
			}
			
			br.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Lưu danh sách tài khoản xuống file
	 */
	private void saveAccounts() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(datapath), "utf8");
		} catch (Exception ex ) {
			System.out.println(ex.getMessage());
		}
		for (Handler client : clients) {
			pw.print(client.getUsername() + "," + client.getPassword() + "\n");
		}
		pw.println("");
		if (pw != null) {
			pw.close();
		}
	}
	
	public Server() throws IOException {
		try {
			// Object dùng để synchronize cho việc giao tiếp với các người dùng
			lock = new Object();
			// Đọc danh sách tài khoản đã đăng ký
			this.loadAccounts();
			//Khởi tạo server với port = 2002
			server = new ServerSocket(9999);
			while (true) {
				//Server bắt đầu nhận tín hiệu từ các client socket
				socket = server.accept();
				DataInputStream is = new DataInputStream(socket.getInputStream());
				DataOutputStream os = new DataOutputStream(socket.getOutputStream());
				// Đọc yêu cầu đăng nhập/đăng xuất
				String request = is.readUTF();
				if (request.equals("Sign up")) {
					// Yêu cầu đăng ký từ user
					String username = is.readUTF();
					String password = is.readUTF();
					// Kiểm tra tên đăng nhập đã tồn tại hay chưa
					if (isExisted(username) == false) {
						// Tạo một Handler để giải quyết các request từ user này
						Handler newHandler = new Handler(socket, username, password, true, lock);
						clients.add(newHandler);
						// Lưu tài khoản của user vào file
						this.saveAccounts();
						os.writeUTF("Sign up successful");
						os.flush();
						// Tạo một Thread cho user 
						Thread tr = new Thread(newHandler);
						tr.start();
						//Cập nhật danh sách trực tuyến
						updateOnlineUsers();
					} else {
						
						// Thông báo đăng nhập thất bại
						os.writeUTF("This username is being used");
						os.flush();
					}
				} else if (request.equals("Log in")) {
					String username = is.readUTF();
					String password = is.readUTF();
					if (isExisted(username) == true) { // Tài khoản chưa tồn tại
						for (Handler client : clients) {
							if (client.getUsername().equals(username)) {
								// Kiểm tra mật khẩu có trùng khớp không
								if (password.equals(client.getPassword())) {
									// Tạo Handler mới để giải quyết các request từ user này
									Handler newHandler = client;
									newHandler.setSocket(socket);
									newHandler.setIsLoggedIn(true);
									// Thông báo đăng nhập thành công cho người dùng
									os.writeUTF("Log in successful");
									os.flush();
									// Tạo một Thread để giao tiếp với user này
									Thread tr = new Thread(newHandler);
									tr.start();
									// Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
									updateOnlineUsers();
								} else {
									os.writeUTF("Password is not correct");
									os.flush();
								}
								break;
							}
						}
					} else {
						os.writeUTF("This username is not exist");
						os.flush();
					}
				}
				
			}
			
		} catch (Exception ex){
			System.err.println(ex);
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}
	
	/**
	 * Kiểm tra username đã tồn tại hay chưa
	 */
	public boolean isExisted(String name) {
		for (Handler client:clients) {
			if (client.getUsername().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gửi yêu cầu các user đang online cập nhật lại danh sách người dùng trực tuyến
	 * Được gọi mỗi khi có 1 user online hoặc offline
	 */
	public static void updateOnlineUsers() {
		String message = " ";
		for (Handler client:clients) {
			if (client.getIsLoggedIn() == true) {
				message += ",";
				message += client.getUsername();
			}
		}
		for (Handler client:clients) {
			if (client.getIsLoggedIn() == true) {
				try {
					client.getDos().writeUTF("Online users");
					client.getDos().writeUTF(message);
					client.getDos().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
}

/**
 * Luồng riêng dùng để giao tiếp với mỗi user
 */
