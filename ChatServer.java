package jj_1119;

import java.awt.Button;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer extends Frame implements ActionListener{

   
   Button btn_ext; //종료버튼
   TextArea txt_list; //접속 목록 출력
   protected Vector list; //접속한 서버 목록 저장
   
   //생성자
   public ChatServer(String title) {
      super(title); //타이틀바에 출력될 문자열
      list = new Vector(); //벡터 생성
      btn_ext = new Button("서버종료"); //서버 종료 버튼 생성
      btn_ext.addActionListener(this); //이벤트 등록
      txt_list = new TextArea(); // txt_list 생성
      add("Center", txt_list); // 화면 가운데 txt_list 생성
      add("South", btn_ext); // 화면 하단에 서버 종료 버튼 출력
      
      //창의 종료버튼
      addWindowListener(new WindowAdapter() {
    	  @Override
    	  public void windowClosing(WindowEvent e) {
    		  System.exit(0);
    		  }
      });
      
      setSize(400, 400); //화면 크기 설정
      setVisible(true);
      ServerStart(); // 채팅 서버 시작
   }
   //채팅 서버
   public void ServerStart() {
      final int port = 7800; // 채팅 서버 포트 상수 지정
      try {
         ServerSocket ss = new ServerSocket(port); //클라이언트 접속 기다림
         while(true) {
          Socket client = ss.accept();
         txt_list.append(client.getInetAddress().getHostAddress() + "\n");
         ChatHandle ch = new ChatHandle(this, client);
         list.addElement(ch);
         ch.start();
         }
      } catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }
   
   
   @Override
   public void actionPerformed(ActionEvent e) {
      System.exit(0);
      
   }
   
   public void setMsg(String msg) {
      txt_list.append(msg + "\n");

}
   public static void main(String[] args) {
      new ChatServer("채팅 서버");
   }
}

class ChatHandle extends Thread {
   ChatServer server = null;
   Socket client = null;
   BufferedReader br = null;
   PrintWriter pw = null;
   
   public ChatHandle(ChatServer server, Socket client) throws IOException {
      this.server = server;
      this.client = client;
      
      InputStream is = client.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
      OutputStream os = client.getOutputStream();
      pw = new PrintWriter(new OutputStreamWriter(os));
   }
   public void Send_All(String msg) {
      try {
         synchronized (server.list) {
            int size = server.list.size();
            for (int i = 0; i < size; i++) {
               ChatHandle chs = (ChatHandle) server.list.elementAt(i);
               synchronized (chs.pw) {
                  chs.pw.println(msg);
               }
               chs.pw.flush();
            }
            
         }
      }catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }
   public void run() {
      String name = "";
      try {
         name = br.readLine();
         Send_All(name + "님이 새로 입장하셨습니다");
         while (true) {
            String msg = br.readLine();
            String str = client.getInetAddress().getHostName();
            synchronized (server) {
               server.setMsg(str + " : " + msg);
            }
            if (msg.equals("@@Exit")) {
               break;
            } else 
               Send_All(name + " : " + msg);
         }
      } catch (Exception e) {
         server.setMsg(e.getMessage());
      }finally {
         synchronized (server.list) {
            server.list.removeElement(this);
         }
         try {
            br.close();
            pw.close();
            client.close();
         } catch (IOException e) {
            server.setMsg(e.getMessage());
         }
      }
   }
   
}