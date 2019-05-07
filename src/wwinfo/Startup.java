package wwinfo;

import javax.servlet.http.HttpServlet;
import  javax.servlet.ServletContextListener;
import  javax.servlet.ServletContextEvent;

public class Startup extends HttpServlet  implements  ServletContextListener {
	private static final long serialVersionUID = 1L;
	
	// 服务器启动
	public void  contextInitialized(ServletContextEvent sce) {
		GlobalFun.debugOut("Startup contextInitialized");
		String[] params=new String[1];
		params[0]="1";
		Timer.main(params);
		//params[0]="2";
		//Timer.main(params);
	}
	//服务器关闭
	public void  contextDestroyed(ServletContextEvent sce) {

		GlobalFun.debugOut("Startup contextDestroyed");
	}
}
