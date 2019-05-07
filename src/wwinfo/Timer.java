package wwinfo;

import java.io.InputStream;
import java.util.Properties;

public class Timer implements Runnable {
	public String indexStart=null;
	
    @Override
    public void run() {
		GlobalFun.debugOut("start thread " + indexStart);
		String index = indexStart;
    	long spliteTime = 5000;
    	try{

	    	InputStream in = getClass().getClassLoader().getResourceAsStream("jdbc.properties");
	    	Properties properties = new Properties();
	    	properties.load(in);
	    	String splite = properties.getProperty("splitsecond");
	    	if (null == splite || splite.isEmpty())
	    		spliteTime = 5000;
	    	else
	    		spliteTime = Long.parseLong(splite)*1000;
    	}catch(Exception e){
    		e.printStackTrace();
    		return;
    	}
		if (index.compareTo("2")==0){
			GlobalFun.writeSysError("Start SyncStore thread");
		}
    	SettleData sendData = new SettleData();
    	while(true){
    		try{
    			if (index.compareTo("2")==0){
    				GlobalFun.writeSysError("Start call SendData("+index+")");
    			}
    			sendData.SendData(index);
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		try{
    			if (index.compareTo("2")==0){
    				GlobalFun.writeSysError("sleep "+Long.toString(spliteTime));
    			}
    			Thread.sleep(spliteTime);
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}
    }
    
    public static void main(String[] args) {
    	if (null == args)
    		return;
    	if (args.length < 1)
    		return;
    	Timer  newTimer= new Timer();
    	newTimer.indexStart = args[0];
        new Thread(newTimer,"threadsynchis"+args[0]).start();
    }
}
