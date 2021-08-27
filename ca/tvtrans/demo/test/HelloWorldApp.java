/**
 * 
 */
package ca.tvtrans.demo.test;

import ca.tvos.app.Application;
import ca.tvos.base.SysIO;

/**
 * @author peter
 *
 */
public class HelloWorldApp extends Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		deploy(new HelloWorldApp(), args);
	}

	@Override
	public void run() {
		SysIO.println("Hello, world!");		
	}

}
