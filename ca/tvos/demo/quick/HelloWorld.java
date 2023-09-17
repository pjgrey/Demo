/**
 * 
 */
package ca.tvos.demo.quick;

import ca.tvos.app.Application;
import ca.tvos.base.SysIO;

/**
 * 
 */
public class HelloWorld extends Application {

	/**
	 * @param args Command line.
	 */
	public static void main(String[] args) {
		deploy( new HelloWorld(), args);
	}

	@Override
	public void run() {
		SysIO.println("Hello, world!");		
	}

}
