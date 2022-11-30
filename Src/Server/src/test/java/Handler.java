/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author tuanb
 */

class Handler implements Runnable{
	// Object để synchronize các hàm cần thiết
	// Các client đều có chung object này được thừa hưởng từ chính server
	private Object lock;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
        
	private final String username;
	private final String password;
	private boolean isLoggedIn;
	
	public Handler(Socket socket, String username, String password, boolean isLoggedIn, Object lock) throws IOException {
            this.socket = socket;
            this.username = username;
            this.password = password;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.isLoggedIn = isLoggedIn;
            this.lock = lock;
	}
	
	public Handler(String username, String password, boolean isLoggedIn, Object lock) {
            this.username = username;
            this.password = password;
            this.isLoggedIn = isLoggedIn;
            this.lock = lock;
	}
	
	public void setIsLoggedIn(boolean IsLoggedIn) {
            this.isLoggedIn = IsLoggedIn;
	}
	
	public void setSocket(Socket socket) {
            this.socket = socket;
            try {
                    this.dis = new DataInputStream(socket.getInputStream());
                    this.dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                    e.printStackTrace();
            }
	}
	
        // Nếu người dùng offline, đóng socket
	public void closeSocket() {
            if (socket != null) {
                try {
                    socket.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
	}
	
	public boolean getIsLoggedIn() {
            return this.isLoggedIn;
	}
	
	public String getUsername() {
            return this.username;
	}
	
	public String getPassword() {
            return this.password;
	}
	
	public DataOutputStream getDos() {
            return this.dos;
	}
	
	@Override
	public void run() {
            while (true) {
                try {
                    String message = null;
                    // Đọc yêu cầu từ user
                    message = dis.readUTF();
                    // Yêu cầu đăng xuất từ user
                    if (message.equals("Log out")) {
                        // Thông báo cho user có thể đăng xuất
                        dos.writeUTF("Safe to leave");
                        dos.flush();
                        // Đóng socket và chuyển trạng thái thành offline
                        socket.close();
                        this.isLoggedIn = false;
                        // Thông báo cho các user khác cập nhật danh sách người dùng trực tuyến
                        Server.updateOnlineUsers();
                        break;
                    }
                    // Yêu cầu gửi tin nhắn dạng văn bản
                    else if (message.equals("Text")){
                        String receiver = dis.readUTF();
                        String content = dis.readUTF();

                        for (Handler client: Server.clients) {
                            if (client.getUsername().equals(receiver)) {
                                synchronized (lock) {
                                    client.getDos().writeUTF("Text");
                                    client.getDos().writeUTF(this.username);
                                    client.getDos().writeUTF(content);
                                    client.getDos().flush();
                                    break;
                                }
                            }
                        }
                    }
                    // Yêu cầu gửi tin nhắn dạng Emoji
                    else if (message.equals("Emoji")) {
                        String receiver = dis.readUTF();
                        String emoji = dis.readUTF();

                        for (Handler client: Server.clients) {
                            if (client.getUsername().equals(receiver)) {
                                synchronized (lock) {
                                    client.getDos().writeUTF("Emoji");
                                    client.getDos().writeUTF(this.username);
                                    client.getDos().writeUTF(emoji);
                                    client.getDos().flush();
                                    break;
                                }
                            }
                        }
                    }
                    // Yêu cầu gửi File
                    else if (message.equals("File")) {
                        // Đọc các header của tin nhắn gửi file
                        String receiver = dis.readUTF();
                        String filename = dis.readUTF();
                        int size = Integer.parseInt(dis.readUTF());
                        int bufferSize = 2048;
                        byte[] buffer = new byte[bufferSize];

                        for (Handler client: Server.clients) {
                            if (client.getUsername().equals(receiver)) {
                                synchronized (lock) {
                                    client.getDos().writeUTF("File");
                                    client.getDos().writeUTF(this.username);
                                    client.getDos().writeUTF(filename);
                                    client.getDos().writeUTF(String.valueOf(size));
                                    while (size > 0) {
                                        // Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
                                        dis.read(buffer, 0, Math.min(size, bufferSize));
                                        client.getDos().write(buffer, 0, Math.min(size, bufferSize));
                                        size -= bufferSize;
                                    }
                                    client.getDos().flush();
                                    break;
                                }
                            }
                        }
                    }

                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
	}
}

