package me.cursedblackcat.dajibot2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The main class of the program.
 * @author Darren Yip
 *
 */
public class DajiBot {
	public static void main(String[] args) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader("token.txt"));
		String token = bufferedReader.readLine();
		bufferedReader.close();
		
		System.out.println(token);
	}
}
