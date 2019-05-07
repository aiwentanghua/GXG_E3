package wwinfo;

import java.io.IOException;

public class demo {
    public static void main(String[] args) throws IOException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                System.out.println(Thread.currentThread().getId());
                for (int i = 0; i <10000 ; i++) {
                    System.out.println(1);
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getId());
                for (int i = 0; i <10000 ; i++) {
                    System.out.println(2222);
                }
            }
        });
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getId());
                for (int i = 0; i <10000 ; i++) {
                    System.out.println(333333333);
                }
            }
        });
        Thread t4 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getId());
                for (int i = 0; i <10000 ; i++) {
                    System.out.println("4444444444444444");
                }
            }
        });

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}
