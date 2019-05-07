package wwinfo;
import net.sf.json.JSONObject;
public class log {


    public static void main(String[] args) throws Exception {


        final String a="测试线程";

        new Thread(new Runnable() {
            @Override
            public void run() {


                System.out.println(a);
            }
        }).start();


        System.out.println("线程是否启动成功");
    }
}
