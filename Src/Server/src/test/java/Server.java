/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author tuanb
 */
public class Server {
	private ServerSocket server;
	private Socket socket;
        
        private Object lock;
	static ArrayList<Handler> clients = new ArrayList<>();
        
	private String data = "data\\accounts.txt";
	
        // Hàm lấy dữ liệu user từ file data
	private void loadAccounts() {
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(data), "utf8"));
                String info = bReader.readLine();
                while (info != null && !(info.isEmpty())) {
                    clients.add(new Handler(info.split(",")[0], info.split(",")[1], false, lock));
                    info = bReader.readLine();
                }
                bReader.close();
            } catch (IOException ex) {
                    ex.printStackTrace();
            }
	}
        // Hàm kiểm tra tài khoản đã tồn tại
	public boolean isExisted(String name) {
            for (Handler client:clients) {
                if (client.getUsername().equals(name)) {
                    return true;
                }
            }
            return false;
	}
	// Hàm lưu dữ liệu user xuống file data
        private void saveAccounts() {
		PrintWriter pWriter = null;
		try {
                    pWriter = new PrintWriter(new File(data), "utf8");
		} 
                catch (IOException ex){
                    System.out.println(ex.getMessage());
		}
		for (Handler client : clients) {
                    //new
                    pWriter.println(client.getUsername() + "," + client.getPassword() + "\n");
		}
		pWriter.println("");
		if (pWriter != null) pWriter.close();
	}
	
	public Server() throws IOException {
		try {
                    // Object dùng để synchronize cho việc giao tiếp với các người dùng
                    lock = new Object();
                    this.loadAccounts();
                    // Server bắt đầu nhận kết nối
                    while (true) {
                        socket = server.accept();
                        DataInputStream is = new DataInputStream(socket.getInputStream());
                        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                        // Đọc yêu cầu đăng nhập/đăng xuất
                        String request = is.readUTF();
                        switch (request){
                            case "Sign up":{ // Xử lý sign up
                                String username = is.readUTF();
                                String password = is.readUTF();
                                
                                if (isExisted(username) == false) { // Tài khoản chưa tồn tại
                                    // Tạo một Handler để giải quyết các request từ user
                                    Handler newHandler = new Handler(socket, username, password, true, lock);
                                    clients.add(newHandler);

                                    // Lưu danh sách tài khoản xuống file và gửi thông báo đăng nhập thành công cho user
                                    this.saveAccounts();
                                    os.writeUTF("Sign up successful");
                                    os.flush();
                                    // Tạo một Thread để giao tiếp với user
                                    Thread t = new Thread(newHandler);
                                    t.start();
                                    //Cập nhật danh sách người dùng online
                                    updateOnlineUsers();
                                } 
                                else { // Tài khoản đã tồn tại
                                    os.writeUTF("This username is being used");
                                    os.flush();
                                }
                                break;
                            }
                            case "Log in":{
                                String username = is.readUTF();
                                String password = is.readUTF();
                                // Kiểm tra tên đăng nhập có tồn tại hay không
                                if (isExisted(username) == true) {
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
                                            Thread t = new Thread(newHandler);
                                            t.start();

                                            // Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
                                            updateOnlineUsers();
                                            } 
                                            else {
                                                os.writeUTF("Password is not correct");
                                                os.flush();
                                            }
                                            break;
                                        }
                                    }
                                } 
                                else {
                                    os.writeUTF("This username is not exist");
                                    os.flush();
                                } 
                                break;
                            }
                            default:{
                                os.writeUTF("This username is not exist");
                                os.flush();
                            }    
                        }
                        /*
                        if (request.equals("Sign up")) {
                            // Yêu cầu đăng ký từ user
                            String username = is.readUTF();
                            String password = is.readUTF();

                            // Kiểm tra tên đăng nhập đã tồn tại hay chưa
                            if (isExisted(username) == false) {
                                // Tạo một Handler để giải quyết các request từ user này
                                Handler newHandler = new Handler(socket, username, password, true, lock);
                                clients.add(newHandler);

                                // Lưu danh sách tài khoản xuống file và gửi thông báo đăng nhập thành công cho user
                                this.saveAccounts();
                                os.writeUTF("Sign up successful");
                                os.flush();
                                // Tạo một Thread để giao tiếp với user này
                                Thread t = new Thread(newHandler);
                                t.start();
                                // Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
                                updateOnlineUsers();
                            } 
                            else {
                                // Thông báo đăng nhập thất bại
                                os.writeUTF("This username is being used");
                                os.flush();
                            }
                        } 
                        else if (request.equals("Log in")) {
                            String username = is.readUTF();
                            String password = is.readUTF();
                            // Kiểm tra tên đăng nhập có tồn tại hay không
                            if (isExisted(username) == true) {
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
                                        Thread t = new Thread(newHandler);
                                        t.start();

                                        // Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực tuyến
                                        updateOnlineUsers();
                                        } 
                                        else {
                                            os.writeUTF("Password is not correct");
                                            os.flush();
                                        }
                                        break;
                                    }
                                }

                            } 
                            else {
                                os.writeUTF("This username is not exist");
                                os.flush();
                            }
                        }*/
                    }
		} 
                catch (IOException ex){
                    System.err.println(ex);
		} 
                finally {
                    if (server != null) server.close();
		}
	}
	
        // Hàm update danh sách các user online hoặc ofline
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
                        } 
                        catch (IOException e) {
                            System.err.println(e);
                        }
                    }
		}
	}
	
}
